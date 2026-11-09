import React, { useState, useRef, useEffect } from 'react';
import {
  Mic,
  Play,
  Pause,
  Upload,
  Code2,
  Package,
  Layers,
  GitBranch,
  Terminal,
  CheckCircle2,
  AlertCircle,
  Copy,
  ExternalLink,
  Smartphone,
  Music,
  Share2,
  RefreshCw,
  FileCode,
  ShieldAlert
} from 'lucide-react';

export default function App() {
  const [activeTab, setActiveTab] = useState<'simulator' | 'patch' | 'morphe' | 'git'>('simulator');
  const [audioFile, setAudioFile] = useState<File | null>(null);
  const [audioUrl, setAudioUrl] = useState<string | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [duration, setDuration] = useState(14.8);
  const [currentTime, setCurrentTime] = useState(0);
  const [waveform, setWaveform] = useState<number[]>([
    25, 30, 42, 65, 80, 92, 75, 60, 45, 38, 55, 78, 88, 95, 82, 68,
    45, 30, 22, 35, 50, 70, 85, 90, 78, 62, 48, 35, 40, 60, 75, 85,
    90, 78, 55, 42, 35, 52, 68, 82, 88, 75, 60, 45, 30, 25, 40, 58,
    72, 85, 90, 78, 62, 48, 35, 30, 45, 60, 75, 82, 70, 55, 40, 25
  ]);
  const [copiedText, setCopiedText] = useState<string | null>(null);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  // Copy helper
  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    setCopiedText(label);
    setTimeout(() => setCopiedText(null), 2000);
  };

  // Handle local MP3 file upload and compute real waveform via Web Audio API
  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setAudioFile(file);
    const url = URL.createObjectURL(file);
    setAudioUrl(url);

    try {
      const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
      const arrayBuffer = await file.arrayBuffer();
      const decodedData = await audioCtx.decodeAudioData(arrayBuffer);

      setDuration(decodedData.duration);
      const channelData = decodedData.getChannelData(0);
      const sampleCount = 64;
      const blockSize = Math.floor(channelData.length / sampleCount);
      const newWaveform: number[] = [];

      for (let i = 0; i < sampleCount; i++) {
        let sum = 0;
        for (let j = 0; j < blockSize; j++) {
          const sample = channelData[i * blockSize + j];
          sum += sample * sample;
        }
        const rms = Math.sqrt(sum / blockSize);
        // Normalize between 15 and 95
        const norm = Math.min(95, Math.max(15, Math.round(rms * 280)));
        newWaveform.push(norm);
      }
      setWaveform(newWaveform);
    } catch (err) {
      console.warn('Could not decode audio with Web Audio API:', err);
    }
  };

  const togglePlay = () => {
    if (!audioRef.current && audioUrl) {
      const audio = new Audio(audioUrl);
      audioRef.current = audio;
      audio.ontimeupdate = () => setCurrentTime(audio.currentTime);
      audio.onended = () => {
        setIsPlaying(false);
        setCurrentTime(0);
      };
    }

    if (audioRef.current) {
      if (isPlaying) {
        audioRef.current.pause();
        setIsPlaying(false);
      } else {
        audioRef.current.play();
        setIsPlaying(true);
      }
    } else {
      // Synthetic playback preview
      setIsPlaying(!isPlaying);
    }
  };

  useEffect(() => {
    let interval: any;
    if (isPlaying && !audioRef.current) {
      interval = setInterval(() => {
        setCurrentTime((prev) => {
          if (prev >= duration) {
            setIsPlaying(false);
            return 0;
          }
          return prev + 0.1;
        });
      }, 100);
    }
    return () => clearInterval(interval);
  }, [isPlaying, duration]);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      {/* Top Header */}
      <header className="border-b border-slate-800 bg-slate-900/60 backdrop-blur sticky top-0 z-40 px-6 py-4 flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-amber-500 via-rose-500 to-purple-600 flex items-center justify-center shadow-lg shadow-rose-500/20">
            <Mic className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-lg font-bold tracking-tight text-white">Instagram MP3 Voice Note</h1>
              <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-rose-500/20 text-rose-400 border border-rose-500/30">
                v1.0.0-experimental.1
              </span>
            </div>
            <p className="text-xs text-slate-400">Target: Instagram 452.1.0.53.64 (code 385811006, ARM64 APKM)</p>
          </div>
        </div>

        {/* Tab Navigation */}
        <nav className="flex items-center gap-1 bg-slate-900 p-1 rounded-xl border border-slate-800">
          <button
            onClick={() => setActiveTab('simulator')}
            className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition-colors flex items-center gap-1.5 ${
              activeTab === 'simulator'
                ? 'bg-rose-500 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <Music className="w-3.5 h-3.5" />
            Audio Simulator
          </button>
          <button
            onClick={() => setActiveTab('patch')}
            className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition-colors flex items-center gap-1.5 ${
              activeTab === 'patch'
                ? 'bg-rose-500 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <Code2 className="w-3.5 h-3.5" />
            Patch Architecture
          </button>
          <button
            onClick={() => setActiveTab('morphe')}
            className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition-colors flex items-center gap-1.5 ${
              activeTab === 'morphe'
                ? 'bg-rose-500 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <Package className="w-3.5 h-3.5" />
            Morphe Config
          </button>
          <button
            onClick={() => setActiveTab('git')}
            className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition-colors flex items-center gap-1.5 ${
              activeTab === 'git'
                ? 'bg-rose-500 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <GitBranch className="w-3.5 h-3.5" />
            Git & Push Sync
          </button>
        </nav>
      </header>

      {/* Main Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-6 space-y-6">
        {/* TAB 1: AUDIO & WAVEFORM SIMULATOR */}
        {activeTab === 'simulator' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
            {/* Left Column: Direct Chat UI Simulation */}
            <div className="lg:col-span-7 space-y-6">
              <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-5">
                <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-full bg-gradient-to-tr from-amber-400 to-rose-600 flex items-center justify-center font-bold text-white text-sm">
                      IG
                    </div>
                    <div>
                      <h2 className="text-sm font-semibold text-white">Instagram Direct Preview</h2>
                      <p className="text-xs text-slate-400">Native Voice Bubble Rendering</p>
                    </div>
                  </div>
                  <span className="text-xs px-2.5 py-1 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full font-mono">
                    Opus Voice Note
                  </span>
                </div>

                {/* Simulated Chat Thread */}
                <div className="bg-slate-950/80 rounded-xl p-5 border border-slate-800/80 min-h-[220px] flex flex-col justify-end">
                  <div className="flex items-start gap-2 max-w-[85%] self-end">
                    {/* Native Voice Bubble */}
                    <div className="bg-gradient-to-r from-purple-600 to-rose-600 text-white p-3.5 rounded-2xl rounded-tr-sm shadow-lg space-y-2.5 w-full">
                      <div className="flex items-center gap-3">
                        {/* Play/Pause Button */}
                        <button
                          onClick={togglePlay}
                          className="w-9 h-9 rounded-full bg-white text-slate-900 flex items-center justify-center shadow-md hover:scale-105 active:scale-95 transition-transform"
                        >
                          {isPlaying ? <Pause className="w-4 h-4 fill-current" /> : <Play className="w-4 h-4 fill-current ml-0.5" />}
                        </button>

                        {/* 64-bar Waveform */}
                        <div className="flex-1 flex items-center gap-[2px] h-9">
                          {waveform.map((amp, idx) => {
                            const progressRatio = currentTime / duration;
                            const barRatio = idx / waveform.length;
                            const isPlayed = barRatio <= progressRatio;

                            return (
                              <div
                                key={idx}
                                style={{ height: `${Math.max(15, (amp / 100) * 100)}%` }}
                                className={`w-1 rounded-full transition-all duration-75 ${
                                  isPlayed ? 'bg-white' : 'bg-white/40'
                                }`}
                              />
                            );
                          })}
                        </div>
                      </div>

                      {/* Duration & Status */}
                      <div className="flex justify-between items-center text-[11px] font-medium text-white/80 px-1">
                        <span>
                          {Math.floor(currentTime / 60)}:{Math.floor(currentTime % 60).toString().padStart(2, '0')} /{' '}
                          {Math.floor(duration / 60)}:{Math.floor(duration % 60).toString().padStart(2, '0')}
                        </span>
                        <span>{audioFile ? audioFile.name : 'Staged MP3 Voice Note'}</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Upload or Choose Custom Audio */}
                <div className="border-2 border-dashed border-slate-800 hover:border-slate-700 rounded-xl p-6 text-center transition-colors">
                  <input
                    type="file"
                    accept="audio/*,.mp3,.ogg,.opus,.m4a"
                    onChange={handleFileUpload}
                    id="audio-upload"
                    className="hidden"
                  />
                  <label htmlFor="audio-upload" className="cursor-pointer flex flex-col items-center gap-2">
                    <div className="w-12 h-12 rounded-full bg-rose-500/10 text-rose-400 flex items-center justify-center">
                      <Upload className="w-5 h-5" />
                    </div>
                    <div>
                      <span className="text-sm font-medium text-white hover:underline">Click to test an MP3 file</span>
                      <p className="text-xs text-slate-400 mt-1">Supports MP3, Opus, Ogg, M4A, AAC</p>
                    </div>
                  </label>
                </div>
              </div>
            </div>

            {/* Right Column: Audio Conversion Pipeline Info */}
            <div className="lg:col-span-5 space-y-6">
              <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
                <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                  <Layers className="w-4 h-4 text-rose-400" />
                  Conversion & Waveform Pipeline
                </h3>
                <p className="text-xs text-slate-400 leading-relaxed">
                  How <code className="text-rose-300">AudioConverter.java</code> prepares selected audio for Instagram Direct:
                </p>

                <div className="space-y-3">
                  <div className="bg-slate-950 p-3.5 rounded-xl border border-slate-800 text-xs space-y-1">
                    <div className="flex items-center justify-between text-slate-300 font-medium">
                      <span>1. MediaMetadata Extraction</span>
                      <span className="text-emerald-400">Passed</span>
                    </div>
                    <p className="text-slate-400">
                      Calculates exact duration ({duration.toFixed(2)}s) via <code className="text-slate-300">MediaMetadataRetriever</code>.
                    </p>
                  </div>

                  <div className="bg-slate-950 p-3.5 rounded-xl border border-slate-800 text-xs space-y-1">
                    <div className="flex items-center justify-between text-slate-300 font-medium">
                      <span>2. Waveform Amplitude Generation</span>
                      <span className="text-emerald-400">{waveform.length} Samples</span>
                    </div>
                    <p className="text-slate-400">
                      Samples RMS audio power normalized to 0–100 byte values for Instagram's Direct bubble visualizer.
                    </p>
                  </div>

                  <div className="bg-slate-950 p-3.5 rounded-xl border border-slate-800 text-xs space-y-1">
                    <div className="flex items-center justify-between text-slate-300 font-medium">
                      <span>3. Container Staging</span>
                      <span className="text-emerald-400">Opus / Direct Cache</span>
                    </div>
                    <p className="text-slate-400">
                      Copies file to <code className="text-slate-300">ig_voice_injected/*.opus</code> ready for dispatch.
                    </p>
                  </div>
                </div>

                <div className="pt-2">
                  <div className="flex items-center justify-between text-xs text-slate-400 mb-1.5">
                    <span>Waveform Array Sample (First 8 bytes):</span>
                    <button
                      onClick={() => copyToClipboard(JSON.stringify(waveform), 'waveform')}
                      className="text-rose-400 hover:text-rose-300 flex items-center gap-1"
                    >
                      <Copy className="w-3 h-3" />
                      {copiedText === 'waveform' ? 'Copied' : 'Copy Full'}
                    </button>
                  </div>
                  <pre className="bg-slate-950 p-3 rounded-xl border border-slate-800 text-[11px] font-mono text-slate-300 overflow-x-auto">
                    {`[${waveform.slice(0, 8).join(', ')}, ... (${waveform.length} bytes)]`}
                  </pre>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: PATCH ARCHITECTURE */}
        {activeTab === 'patch' && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Hook 1 & 2 */}
              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
                <div className="flex items-center gap-2 text-rose-400 font-semibold text-sm">
                  <Smartphone className="w-4 h-4" />
                  Composer UI Hook (VoiceMessagingComposerController)
                </div>
                <p className="text-xs text-slate-400">
                  Located in <code className="text-slate-300">classes10.dex</code>. Injects a long-click listener on the microphone view.
                </p>
                <div className="bg-slate-950 p-4 rounded-xl border border-slate-800 font-mono text-xs text-slate-300 space-y-2">
                  <div className="text-slate-500">// Smali Injection</div>
                  <div className="text-emerald-400">
                    invoke-static &#123; p0, p1 &#125;, Ldev/zangebismefe/instagramvoice/VoiceNoteHook;-&gt;attachComposerHook(Ljava/lang/Object;Landroid/view/View;)V
                  </div>
                </div>
                <ul className="text-xs text-slate-400 space-y-1.5 list-disc list-inside">
                  <li><strong className="text-slate-200">Normal click/hold:</strong> Standard microphone voice recording.</li>
                  <li><strong className="text-slate-200">Long click:</strong> Opens audio picker modal to choose MP3 voice note.</li>
                </ul>
              </div>

              {/* Hook 3 & 4 */}
              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
                <div className="flex items-center gap-2 text-rose-400 font-semibold text-sm">
                  <FileCode className="w-4 h-4" />
                  Recorder & Dispatch Hook (OpusVoiceRecorder)
                </div>
                <p className="text-xs text-slate-400">
                  Located in <code className="text-slate-300">classes21.dex</code> and <code className="text-slate-300">classes4.dex</code>.
                </p>
                <div className="bg-slate-950 p-4 rounded-xl border border-slate-800 font-mono text-xs text-slate-300 space-y-2">
                  <div className="text-slate-500">// Intercept audio path & dispatch</div>
                  <div className="text-emerald-400">
                    invoke-static &#123; v0 &#125;, Ldev/zangebismefe/instagramvoice/VoiceNoteHook;-&gt;interceptAudioFilePath(Ljava/lang/String;)Ljava/lang/String;
                  </div>
                  <div className="text-purple-400">
                    move-result-object v0
                  </div>
                </div>
                <ul className="text-xs text-slate-400 space-y-1.5 list-disc list-inside">
                  <li>Substitutes recorded temp file with staged MP3 audio.</li>
                  <li>Supplies verified duration and waveform to Direct server upload.</li>
                  <li>Resets staged state after successful transmission.</li>
                </ul>
              </div>
            </div>

            {/* Source Code Tree */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
              <h3 className="text-sm font-semibold text-white">Repository Implementation Tree</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
                <div className="bg-slate-950 p-4 rounded-xl border border-slate-800 space-y-2">
                  <div className="font-semibold text-slate-200 flex items-center gap-1.5">
                    <Package className="w-4 h-4 text-rose-400" />
                    Android Extension (extensions/extension/)
                  </div>
                  <div className="space-y-1 font-mono text-slate-400 pl-4 border-l border-slate-800">
                    <div>├── AudioPickerActivity.java</div>
                    <div>├── AudioConverter.java</div>
                    <div>├── VoiceNoteHook.java</div>
                    <div>├── VoiceNoteState.java</div>
                    <div>└── AndroidManifest.xml</div>
                  </div>
                </div>

                <div className="bg-slate-950 p-4 rounded-xl border border-slate-800 space-y-2">
                  <div className="font-semibold text-slate-200 flex items-center gap-1.5">
                    <Code2 className="w-4 h-4 text-rose-400" />
                    Morphe Patch (patches/)
                  </div>
                  <div className="space-y-1 font-mono text-slate-400 pl-4 border-l border-slate-800">
                    <div>├── InstagramTarget.kt</div>
                    <div>├── InstagramVoicePatch.kt</div>
                    <div>├── PatchListGenerator.kt</div>
                    <div>└── build.gradle.kts</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: MORPHE CONFIG & MANAGER */}
        {activeTab === 'morphe' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
            <div className="lg:col-span-7 space-y-6">
              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4 shadow-xl">
                <div className="flex items-center justify-between">
                  <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                    <Package className="w-4 h-4 text-rose-400" />
                    patches-list.json
                  </h3>
                  <button
                    onClick={() =>
                      copyToClipboard(
                        JSON.stringify(
                          {
                            version: '1.0.0-experimental.1',
                            patches: [
                              {
                                name: 'Instagram MP3 Voice Note',
                                compatiblePackages: [
                                  {
                                    packageName: 'com.instagram.android',
                                    targets: [{ version: '452.1.0.53.64', isExperimental: true }]
                                  }
                                ]
                              }
                            ]
                          },
                          null,
                          2
                        ),
                        'patches-json'
                      )
                    }
                    className="text-xs text-rose-400 hover:text-rose-300 flex items-center gap-1"
                  >
                    <Copy className="w-3 h-3" />
                    {copiedText === 'patches-json' ? 'Copied' : 'Copy'}
                  </button>
                </div>
                <pre className="bg-slate-950 p-4 rounded-xl border border-slate-800 text-xs font-mono text-slate-300 overflow-x-auto">
{`{
  "version": "1.0.0-experimental.1",
  "patches": [
    {
      "name": "Instagram MP3 Voice Note",
      "description": "Allows selecting a local MP3 audio file and sending it as a native Instagram Direct voice message instead of an attachment.",
      "default": true,
      "category": "Chat",
      "compatiblePackages": [
        {
          "packageName": "com.instagram.android",
          "name": "Instagram",
          "apkFileType": "APKM",
          "appIconColor": "#E1306C",
          "targets": [
            {
              "version": "452.1.0.53.64",
              "isExperimental": true,
              "minSdk": 28
            }
          ]
        }
      ]
    }
  ]
}`}
                </pre>
              </div>
            </div>

            <div className="lg:col-span-5 space-y-6">
              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4 shadow-xl">
                <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                  <Smartphone className="w-4 h-4 text-rose-400" />
                  Morphe Manager Setup
                </h3>
                <p className="text-xs text-slate-400 leading-relaxed">
                  To use this repository in Morphe Manager on your phone:
                </p>

                <div className="space-y-3 text-xs">
                  <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-rose-500/20 text-rose-400 flex items-center justify-center font-bold text-[10px]">
                      1
                    </span>
                    <div>
                      <strong className="text-slate-200">Add Patch Source</strong>
                      <p className="text-slate-400 mt-0.5">
                        In Morphe Manager, open Settings &gt; Sources &gt; Add Source, and enter:
                      </p>
                      <code className="block mt-1 p-1.5 bg-slate-900 rounded text-slate-300 text-[11px] select-all">
                        https://github.com/zangebismefe/instagram-mp3-voice-note
                      </code>
                    </div>
                  </div>

                  <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-rose-500/20 text-rose-400 flex items-center justify-center font-bold text-[10px]">
                      2
                    </span>
                    <div>
                      <strong className="text-slate-200">Select Instagram APKM</strong>
                      <p className="text-slate-400 mt-0.5">
                        Provide the ARM64 APKM for version <strong>452.1.0.53.64</strong> (code 385811006).
                      </p>
                    </div>
                  </div>

                  <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-rose-500/20 text-rose-400 flex items-center justify-center font-bold text-[10px]">
                      3
                    </span>
                    <div>
                      <strong className="text-slate-200">Patch & Install</strong>
                      <p className="text-slate-400 mt-0.5">
                        Morphe merges <code className="text-slate-300">extensions/extension.mpe</code> and applies bytecode hooks.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: GIT & PUSH SYNC */}
        {activeTab === 'git' && (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center">
                  <GitBranch className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-sm font-semibold text-white">Local Git Commit Status</h3>
                  <p className="text-xs text-slate-400">Clean working tree on branch <code className="text-purple-400">dev</code></p>
                </div>
              </div>
              <span className="px-3 py-1 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full text-xs font-medium">
                1 Commit Ahead of origin/main
              </span>
            </div>

            <div className="bg-slate-950 p-4 rounded-xl border border-slate-800 font-mono text-xs text-slate-300 space-y-1">
              <div className="text-slate-500">commit f78c7aa (HEAD -&gt; dev)</div>
              <div>Author: zangebismefe &lt;qwe71323@gmail.com&gt;</div>
              <div>Date:   Mon Nov 9 2026</div>
              <div className="text-emerald-400 font-bold mt-2">
                feat: implement native voice note patch and extension for Instagram 452.1.0.53.64
              </div>
              <div className="text-slate-400 mt-1">13 files changed, 758 insertions(+), 49 deletions(-)</div>
            </div>

            <div className="space-y-4">
              <h4 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
                <Terminal className="w-4 h-4 text-rose-400" />
                Push to GitHub via Terminal
              </h4>
              <p className="text-xs text-slate-400">
                To publish this commit and trigger the automatic GitHub Actions release workflow:
              </p>

              <div className="bg-slate-950 p-4 rounded-xl border border-slate-800 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-xs text-slate-400">Using Personal Access Token (PAT):</span>
                  <button
                    onClick={() =>
                      copyToClipboard(
                        'git push https://<YOUR_GITHUB_TOKEN>@github.com/zangebismefe/instagram-mp3-voice-note.git dev',
                        'push-cmd'
                      )
                    }
                    className="text-xs text-rose-400 hover:text-rose-300 flex items-center gap-1"
                  >
                    <Copy className="w-3 h-3" />
                    {copiedText === 'push-cmd' ? 'Copied' : 'Copy Command'}
                  </button>
                </div>
                <pre className="text-xs font-mono text-purple-300 overflow-x-auto">
                  git push https://&lt;YOUR_GITHUB_TOKEN&gt;@github.com/zangebismefe/instagram-mp3-voice-note.git dev
                </pre>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800 bg-slate-900/40 px-6 py-4 text-center text-xs text-slate-500">
        Instagram MP3 Voice Note • Morphe Android Research & Studio • dev branch
      </footer>
    </div>
  );
}
