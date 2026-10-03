'use client';

import React, { useState } from 'react';
import { Shield, Lock, Mail, KeyRound, AlertCircle, ArrowRight, UserCheck } from 'lucide-react';
import { loginOperator, quickDemoLogin, SOCOperator } from '@/lib/auth';

interface LoginModalProps {
  onLoginSuccess: (operator: SOCOperator) => void;
}

export const LoginModal: React.FC<LoginModalProps> = ({ onLoginSuccess }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const res = await loginOperator(email, password);
      setIsLoading(false);
      if (res.success && res.operator) {
        onLoginSuccess(res.operator);
      } else {
        setError(res.error || 'Autentikasi gagal.');
      }
    } catch (err: unknown) {
      setIsLoading(false);
      setError(err instanceof Error ? err.message : 'Terjadi kesalahan saat login.');
    }
  };

  const handleQuickLogin = (role: 'admin' | 'analyst' | 'helpdesk') => {
    setIsLoading(true);
    setTimeout(() => {
      const op = quickDemoLogin(role);
      setIsLoading(false);
      onLoginSuccess(op);
    }, 300);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#070c18]/80 backdrop-blur-md p-4">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-[#e2e8f0] overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        {/* Header Branding */}
        <div className="bg-gradient-to-r from-[#be001c] via-[#d90429] to-[#8d0801] p-6 text-white text-center relative">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-white/10 backdrop-blur-sm border border-white/20 mb-3 shadow-inner">
            <Shield className="w-8 h-8 text-white" />
          </div>
          <h2 className="text-xl font-bold tracking-tight">TELKOMSEL SECURE SOC</h2>
          <p className="text-xs text-red-100 font-medium mt-1">
            Security Operations Center &amp; Threat Intelligence Gateway
          </p>
          <div className="absolute top-3 right-3 text-[10px] bg-black/20 border border-white/20 px-2 py-0.5 rounded-full font-mono text-red-100">
            v2.4 SECURE
          </div>
        </div>

        {/* Form Body */}
        <div className="p-6 space-y-5">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-xl flex items-center gap-2.5 text-xs text-red-700">
              <AlertCircle className="w-4 h-4 shrink-0 text-red-600" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Email Operator SOC
              </label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="analyst@telkomsel.co.id"
                  required
                  className="w-full pl-10 pr-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#be001c]/30 focus:border-[#be001c] transition-all"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Kata Sandi
              </label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  className="w-full pl-10 pr-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#be001c]/30 focus:border-[#be001c] transition-all"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full flex items-center justify-center gap-2 py-2.5 px-4 bg-gradient-to-r from-[#be001c] to-[#ed0226] hover:from-[#a00018] hover:to-[#be001c] text-white rounded-xl text-xs font-bold transition-all shadow-md shadow-red-900/10 active:scale-[0.98] disabled:opacity-50"
            >
              {isLoading ? (
                <span>Memverifikasi Sesi...</span>
              ) : (
                <>
                  <KeyRound className="w-4 h-4" />
                  <span>Masuk ke Konsol SOC</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          {/* Quick Demo Logins */}
          <div className="pt-4 border-t border-slate-100">
            <p className="text-[11px] font-semibold text-slate-500 mb-2.5 text-center">
              Akses Cepat (Demo Single Sign-On):
            </p>
            <div className="grid grid-cols-3 gap-2 text-center">
              <button
                type="button"
                onClick={() => handleQuickLogin('admin')}
                className="p-2 bg-slate-50 hover:bg-red-50 border border-slate-200 hover:border-red-200 rounded-lg text-[10px] font-bold text-slate-700 hover:text-red-700 transition-all flex flex-col items-center gap-1"
              >
                <Shield className="w-3.5 h-3.5 text-red-600" />
                <span>Admin</span>
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('analyst')}
                className="p-2 bg-slate-50 hover:bg-blue-50 border border-slate-200 hover:border-blue-200 rounded-lg text-[10px] font-bold text-slate-700 hover:text-blue-700 transition-all flex flex-col items-center gap-1"
              >
                <UserCheck className="w-3.5 h-3.5 text-blue-600" />
                <span>SOC Analyst</span>
              </button>
              <button
                type="button"
                onClick={() => handleQuickLogin('helpdesk')}
                className="p-2 bg-slate-50 hover:bg-emerald-50 border border-slate-200 hover:border-emerald-200 rounded-lg text-[10px] font-bold text-slate-700 hover:text-emerald-700 transition-all flex flex-col items-center gap-1"
              >
                <Mail className="w-3.5 h-3.5 text-emerald-600" />
                <span>Helpdesk</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
