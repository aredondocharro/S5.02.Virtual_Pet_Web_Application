import React from "react";

type AuthLayoutProps = {
  children: React.ReactNode;
  image?: string; // opcional: personalizar fondo por página
};

export default function AuthLayout({ children, image = "/backgrounds/clean-axolotl-underwater.png" }: AuthLayoutProps) {
  return (
    <div
      className="min-h-screen grid place-items-center bg-center bg-cover md:bg-fixed relative p-6"
      style={{ backgroundImage: `url('${image}')` }}
    >
      <div
        className="absolute inset-0 pointer-events-none
        [background:radial-gradient(60%_40%_at_20%_10%,rgba(255,105,180,.15),transparent_60%),
                    radial-gradient(80%_60%_at_80%_10%,rgba(56,189,248,.12),transparent_60%),
                    linear-gradient(to_bottom,rgba(0,0,0,.35),rgba(0,0,0,.65))]"
        aria-hidden
      />
      <div className="relative z-10 w-full max-w-md">
        {children}
      </div>
    </div>
  );
}
