/**
 * Professional SOC audio chime for critical security events using native Web Audio API.
 * Requires zero external audio files.
 */
let audioCtx: AudioContext | null = null;

const SOUND_STORAGE_KEY = 'tsel_soc_sound_enabled';

export function isSoundEnabled(): boolean {
  if (typeof window === 'undefined') return true;
  const stored = localStorage.getItem(SOUND_STORAGE_KEY);
  return stored !== 'false';
}

export function setSoundEnabled(enabled: boolean): void {
  if (typeof window === 'undefined') return;
  localStorage.setItem(SOUND_STORAGE_KEY, enabled ? 'true' : 'false');
}

export function playCriticalThreatAlert(): void {
  if (typeof window === 'undefined' || !isSoundEnabled()) return;

  try {
    const AudioCtxClass =
      window.AudioContext ||
      (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
    if (!AudioCtxClass) return;

    if (!audioCtx || audioCtx.state === 'suspended') {
      audioCtx = new AudioCtxClass();
    }

    const ctx = audioCtx;
    const now = ctx.currentTime;

    // Dual-tone security chime: Tone 1 (High chime) then Tone 2 (Higher alert chime)
    const osc1 = ctx.createOscillator();
    const osc2 = ctx.createOscillator();
    const gainNode = ctx.createGain();

    osc1.type = 'sine';
    osc1.frequency.setValueAtTime(880, now); // A5 note
    osc1.frequency.exponentialRampToValueAtTime(1100, now + 0.12);

    osc2.type = 'triangle';
    osc2.frequency.setValueAtTime(1320, now + 0.12); // E6 note
    osc2.frequency.exponentialRampToValueAtTime(1760, now + 0.35); // A6 note

    // Smooth envelope attack and exponential decay
    gainNode.gain.setValueAtTime(0.01, now);
    gainNode.gain.linearRampToValueAtTime(0.18, now + 0.05);
    gainNode.gain.exponentialRampToValueAtTime(0.001, now + 0.45);

    osc1.connect(gainNode);
    osc2.connect(gainNode);
    gainNode.connect(ctx.destination);

    osc1.start(now);
    osc1.stop(now + 0.15);

    osc2.start(now + 0.12);
    osc2.stop(now + 0.45);
  } catch (err) {
    // Non-intrusive fallback if autoplay is restricted by browser policy
    console.debug('[SoundAlert] AudioContext notice:', err);
  }
}
