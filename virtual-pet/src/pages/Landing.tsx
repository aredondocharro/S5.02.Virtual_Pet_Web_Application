import { Link } from "react-router-dom";
import { Button } from "../components/ui/button";
import { Heart, Gamepad2, Utensils, Moon, Sparkles } from "lucide-react";

export default function Landing() {
  return (
<div
  className="relative min-h-[100svh] bg-cover bg-center bg-no-repeat md:bg-fixed"
  style={{ backgroundImage: "url(/backgrounds/landing-bg.png)" }}
  aria-label="Axolotl underwater background"
>
{/* Bubbles overlay: FIXED al viewport y por encima del fondo */}
<div className="fixed inset-0 overflow-hidden pointer-events-none z-0" aria-hidden>
  {/* burbujas pequeñas */}
  <span className="bubble animate-bubble" style={{ left: "6%",  width: "12px", height: "12px", animationDelay: "0.2s",  animationDuration: "11s" }} />
  <span className="bubble animate-bubble" style={{ left: "16%", width: "12px", height: "12px", animationDelay: "0.9s",  animationDuration: "10.5s" }} />
  <span className="bubble animate-bubble" style={{ left: "28%", width: "12px", height: "12px", animationDelay: "0.6s",  animationDuration: "10s" }} />
  <span className="bubble animate-bubble" style={{ left: "41%", width: "12px", height: "12px", animationDelay: "1.5s",  animationDuration: "12.5s" }} />
  <span className="bubble animate-bubble" style={{ left: "55%", width: "12px", height: "12px", animationDelay: "0.3s",  animationDuration: "11.5s" }} />
  <span className="bubble animate-bubble" style={{ left: "67%", width: "12px", height: "12px", animationDelay: "1.2s",  animationDuration: "12.2s" }} />
  <span className="bubble animate-bubble" style={{ left: "79%", width: "12px", height: "12px", animationDelay: "0.4s",  animationDuration: "10.8s" }} />
  <span className="bubble animate-bubble" style={{ left: "91%", width: "12px", height: "12px", animationDelay: "1.0s",  animationDuration: "12s" }} />

  {/* burbujas medianas (más visibles) */}
  <span className="bubble animate-bubble" style={{ left: "22%", width: "16px", height: "16px", animationDelay: "0.7s", animationDuration: "13s" }} />
  <span className="bubble animate-bubble" style={{ left: "48%", width: "16px", height: "16px", animationDelay: "1.3s", animationDuration: "12.8s" }} />

  {/* burbujas grandes ocasionales (muy visibles) */}
  <span className="bubble animate-bubble" style={{ left: "35%", width: "22px", height: "22px", animationDelay: "2s",   animationDuration: "15s" }} />
  <span className="bubble animate-bubble" style={{ left: "74%", width: "20px", height: "20px", animationDelay: "1.6s", animationDuration: "14s" }} />
</div>

      {/* HEADER (contenido por encima de las burbujas) */}
      <header className="relative z-[100] border-b border-white/40 bg-teal-900/60 backdrop-blur-md sticky top-0">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-pink-500 rounded-full flex items-center justify-center shadow-lg">
              <span className="text-white font-bold text-sm" aria-hidden>🦎</span>
            </div>
            <span className="font-bold text-xl text-white drop-shadow-[2px_2px_4px_rgba(0,0,0,0.8)]">
              AXOLOTL PROJECT
            </span>
          </div>

          <div className="flex items-center gap-2">
            <Link to="/login" aria-label="Go to login">
              <Button
                variant="ghost"
                size="sm"
                className="text-white hover:bg-white/30 border border-white/50 backdrop-blur-sm"
              >
                Login
              </Button>
            </Link>

            <Link to="/register" aria-label="Go to register">
              <Button
                size="sm"
                className="bg-pink-500 hover:bg-pink-600 text-white shadow-lg border border-pink-400"
              >
                Register
              </Button>
            </Link>
          </div>
        </div>
      </header>

      {/* HERO */}
      <section className="relative z-10 py-20 px-4">
        <div className="container mx-auto text-center max-w-3xl">
          <h1 className="text-4xl md:text-5xl font-bold mb-6 text-white drop-shadow-[3px_3px_6px_rgba(0,0,0,0.8)]">
            Your Virtual{" "}
            <span className="text-yellow-200 drop-shadow-[2px_2px_4px_rgba(0,0,0,0.9)]">
              Axolotl Friend
            </span>
          </h1>

          <p className="text-lg text-white mb-8 max-w-xl mx-auto drop-shadow-[2px_2px_4px_rgba(0,0,0,0.7)] bg-teal-800/30 backdrop-blur-sm rounded-lg p-4 border border-white/20">
            Care for your own axolotl tamagotchi! Feed them, play together, let them rest, and watch them evolve while
            learning about these amazing endangered creatures.
          </p>

          <div className="relative max-w-sm mx-auto mb-12">
            <img
              src="/pictures/cute-swimming-axolotl.png"
              alt="Cute axolotl swimming with bubbles"
              className="
                w-full h-auto select-none will-change-transform
                [animation:axofloat_4s_ease-in-out_infinite]
                hover:[animation:axofloat_3.5s_ease-in-out_infinite]
                motion-reduce:[animation:none]
              "
              draggable={false}
            />
            <div className="absolute -top-2 -right-2 bg-pink-500 text-white px-3 py-1 rounded-full text-sm font-medium animate-bounce flex items-center gap-1 shadow-xl border-2 border-white/30">
              <Heart className="h-4 w-4" aria-hidden />
              Adopt Me!
            </div>
          </div>

          {/* Feature Pills */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-12">
            <Feature icon={<Gamepad2 className="h-6 w-6 text-yellow-900" />} label="Play"   box="bg-yellow-400/80" ring="border-yellow-300" />
            <Feature icon={<Utensils className="h-6 w-6 text-pink-900" />}  label="Feed"   box="bg-pink-400/80"   ring="border-pink-300" />
            <Feature icon={<Moon className="h-6 w-6 text-cyan-900" />}      label="Rest"   box="bg-cyan-300/80"   ring="border-cyan-200" />
            <Feature icon={<Sparkles className="h-6 w-6 text-gray-700" />}  label="Evolve" box="bg-white/90"      ring="border-gray-200" />
          </div>

          {/* Primary CTA */}
          <Link to="/register" aria-label="Start playing now by registering">
            <Button
              size="lg"
              className="text-lg px-8 mb-8 bg-pink-500 hover:bg-pink-600 text-white shadow-xl border-2 border-pink-400"
            >
              Start Playing Now
            </Button>
          </Link>
        </div>
      </section>

      {/* INFO STRIP */}
      <section className="relative z-10 py-16 px-4 bg-teal-900/70 backdrop-blur-md border-t border-white/30">
        <div className="container mx-auto text-center max-w-2xl">
          <h2 className="text-2xl font-bold mb-4 text-white drop-shadow-[3px_3px_6px_rgba(0,0,0,0.8)]">
            Help Save Real Axolotls
          </h2>
        <p className="text-white mb-6 drop-shadow-[2px_2px_4px_rgba(0,0,0,0.7)] bg-teal-800/40 rounded-lg p-4 border border-white/20">
            Born of Mexico City’s ancient lakes, the axolotl is a forever-young salamander with feathery gills and a quiet superpower: it can regrow limbs—and even parts of its heart and brain. Today, this wonder is critically endangered in the wild. By protecting clean water and the wetlands of Xochimilco, we can keep that small, brave smile in our world.
          </p>
          <div className="text-sm text-pink-200 drop-shadow-[1px_1px_2px_rgba(0,0,0,0.8)]">
            Made with 💖 by Alejandro Redondo Charro
          </div>
        </div>
      </section>
    </div>
  );
}

function Feature({
  icon, label, box, ring,
}: { icon: React.ReactNode; label: string; box: string; ring: string }) {
  return (
    <div className="text-center">
      <div className={`w-12 h-12 ${box} backdrop-blur-sm rounded-full flex items-center justify-center mx-auto mb-2 border-2 ${ring} shadow-lg`}>
        {icon}
      </div>
      <p className="text-sm font-medium text-white drop-shadow-[2px_2px_4px_rgba(0,0,0,0.8)] bg-teal-800/40 rounded px-2 py-1">
        {label}
      </p>
    </div>
  );
}

