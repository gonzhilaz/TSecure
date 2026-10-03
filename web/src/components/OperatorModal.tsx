'use client';

import React, { useState } from 'react';
import { X, UserPlus, Lock, Mail, User, Shield, Hash } from 'lucide-react';
import { createOperator } from '@/lib/api';
import { Operator } from '@/types';

interface OperatorModalProps {
  onClose: () => void;
  onOperatorCreated: (op: Operator) => void;
}

export const OperatorModal: React.FC<OperatorModalProps> = ({ onClose, onOperatorCreated }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('SOC_ANALYST');
  const [badgeNumber, setBadgeNumber] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const res = await createOperator({
        name,
        email,
        password,
        role,
        badge_number: badgeNumber,
      });
      setIsLoading(false);
      onOperatorCreated(res.operator);
      onClose();
    } catch (err: unknown) {
      setIsLoading(false);
      setError(err instanceof Error ? err.message : 'Gagal membuat operator');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-fadeIn">
      <div className="bg-white rounded-2xl max-w-md w-full border border-[#e9bcb8]/80 shadow-2xl overflow-hidden">
        <div className="flex items-center justify-between p-5 border-b border-[#e9bcb8]/60 bg-[#fff8f7]">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-[#ffe9e7] text-[#be001c]">
              <UserPlus className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-base text-[#0b132b]">Tambah Operator SOC</h3>
              <p className="text-[11px] text-[#778ca2]">Registrasi personel & hak akses RBAC</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-[#778ca2] hover:bg-white hover:text-[#0b132b] transition-all"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-3.5">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-xs text-red-700">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-[#0b132b] mb-1">Nama Lengkap</label>
            <div className="relative">
              <User className="w-4 h-4 text-[#778ca2] absolute left-3 top-2.5" />
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Rahmat Hidayat"
                className="w-full pl-9 pr-3 py-2 text-xs bg-[#fff8f7] border border-[#e9bcb8] rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#ed0226]/20"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#0b132b] mb-1">Email Telkomsel (@telkomsel.co.id)</label>
            <div className="relative">
              <Mail className="w-4 h-4 text-[#778ca2] absolute left-3 top-2.5" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="rahmat_h@telkomsel.co.id"
                className="w-full pl-9 pr-3 py-2 text-xs bg-[#fff8f7] border border-[#e9bcb8] rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#ed0226]/20"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#0b132b] mb-1">Kata Sandi Awal</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-[#778ca2] absolute left-3 top-2.5" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Minimal 8 karakter"
                className="w-full pl-9 pr-3 py-2 text-xs bg-[#fff8f7] border border-[#e9bcb8] rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#ed0226]/20"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-[#0b132b] mb-1">Role / Peran RBAC</label>
              <div className="relative">
                <Shield className="w-4 h-4 text-[#778ca2] absolute left-3 top-2.5 pointer-events-none" />
                <select
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                  className="w-full pl-9 pr-2 py-2 text-xs bg-[#fff8f7] border border-[#e9bcb8] rounded-xl focus:bg-white focus:outline-none font-medium"
                >
                  <option value="SOC_ANALYST">SOC Analyst</option>
                  <option value="CUSTOMER_CARE">Customer Care</option>
                  <option value="AUDITOR">Auditor (View Only)</option>
                  <option value="SUPERADMIN">Super Admin</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-[#0b132b] mb-1">Badge ID</label>
              <div className="relative">
                <Hash className="w-4 h-4 text-[#778ca2] absolute left-3 top-2.5" />
                <input
                  type="text"
                  value={badgeNumber}
                  onChange={(e) => setBadgeNumber(e.target.value)}
                  placeholder="TS-SOC-7701"
                  className="w-full pl-9 pr-3 py-2 text-xs bg-[#fff8f7] border border-[#e9bcb8] rounded-xl focus:bg-white focus:outline-none"
                />
              </div>
            </div>
          </div>

          <div className="flex items-center justify-end gap-2 pt-3 border-t border-[#e9bcb8]/60">
            <button
              type="button"
              onClick={onClose}
              className="px-3.5 py-1.5 text-xs font-semibold text-[#778ca2] hover:text-[#0b132b] bg-white border border-[#e9bcb8] rounded-xl hover:bg-gray-50"
            >
              Batal
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="px-4 py-1.5 text-xs font-semibold text-white bg-[#ed0226] hover:bg-[#be001c] rounded-xl shadow-sm transition-all disabled:opacity-50"
            >
              {isLoading ? 'Menyimpan...' : 'Simpan Operator'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
