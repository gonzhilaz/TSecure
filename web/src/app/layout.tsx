import type { Metadata } from 'next';
import React from 'react';
import { Public_Sans } from 'next/font/google';
import './globals.css';

const publicSans = Public_Sans({
  subsets: ['latin'],
  weight: ['400', '500', '600', '700'],
  variable: '--font-public-sans',
});

export const metadata: Metadata = {
  title: 'Telkomsel Secure | Cyber SOC & Licensing Gateway',
  description:
    'Security Operations Center (SOC) Executive Dashboard & NDP Entitlement Gateway for Telkomsel Secure and Kaspersky Mobile Security.',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="id">
      <body className={`${publicSans.className} antialiased bg-[#fff8f7] text-[#2a1615] min-h-screen`}>
        {children}
      </body>
    </html>
  );
}
