import { FormEvent, useState } from "react";
import { useNavigate, Link, useLocation } from "react-router-dom";
import { Heart, Eye, EyeOff } from "lucide-react";

import { apiPost } from "../api/client";
import { useAuth } from "../context/AuthContext";

import { Input } from "../components/ui/input";
import { Button } from "../components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../components/ui/card";

type AnyAuth = { token?: string; jwt?: string; accessToken?: string };

export default function Login() {
  const nav = useNavigate();
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const reason = params.get("reason");

  const { setToken } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPwd, setShowPwd] = useState(false);

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (!email || !password) throw new Error("Please fill in all fields");

      const res = await apiPost<AnyAuth>("/auth/login", { email, password }, false);
      const tok = res.token ?? res.jwt ?? res.accessToken;
      if (!tok) throw new Error("Login OK but token was not returned. Check backend AuthResponse.");

      setToken(tok);
      nav("/app", { replace: true });
    } catch (err: any) {
      setError(err?.message || "Invalid credentials");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div
      className="min-h-screen grid place-items-center bg-center bg-cover md:bg-fixed relative p-6"
      style={{ backgroundImage: "url('/backgrounds/clean-axolotl-underwater.png')" }}
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
      {/* Overlay for readability */}
      <div
        className="absolute inset-0 pointer-events-none
        [background:radial-gradient(60%_40%_at_20%_10%,rgba(255,105,180,.15),transparent_60%),
                    radial-gradient(80%_60%_at_80%_10%,rgba(56,189,248,.12),transparent_60%),
                    linear-gradient(to_bottom,rgba(0,0,0,.35),rgba(0,0,0,.65))]"
        aria-hidden
      />

      <div className="relative z-10 w-full max-w-md">
        <Card className="w-full bg-white/90 backdrop-blur-sm border-2 border-pink-300/50 shadow-xl">
          <CardHeader className="text-center">
            <div className="flex justify-center mb-2">
              <Heart className="h-8 w-8 text-pink-500 fill-pink-500" />
            </div>
            <CardTitle className="text-2xl font-bold text-teal-700">Welcome Back!</CardTitle>
            <CardDescription className="text-teal-600">
              Your axolotl friend is waiting for you
            </CardDescription>
          </CardHeader>

          <CardContent>
            {/* Reason banners */}
            {reason === "expired" && (
              <p className="mb-3 text-sm text-amber-800 bg-amber-100 border border-amber-200 rounded px-3 py-2">
                Your session has expired. Please sign in again.
              </p>
            )}
            {reason === "registered" && (
              <p className="mb-3 text-sm text-emerald-800 bg-emerald-100 border border-emerald-200 rounded px-3 py-2">
                Account created successfully. Please sign in.
              </p>
            )}

            <form onSubmit={onSubmit} className="space-y-4">
              <Input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Email"
                className="border-teal-300 focus:border-pink-400 focus:ring-pink-400"
                required
                autoComplete="email"
              />

              <div className="relative">
                <Input
                  type={showPwd ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Password"
                  className="pr-10 border-teal-300 focus:border-pink-400 focus:ring-pink-400"
                  required
                  autoComplete="current-password"
                  minLength={4}
                />
                <button
                  type="button"
                  onClick={() => setShowPwd((v) => !v)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 p-1 rounded hover:bg-gray-100 focus:outline-none"
                  aria-label={showPwd ? "Hide password" : "Show password"}
                >
                  {showPwd ? <EyeOff className="h-5 w-5 text-gray-500" /> : <Eye className="h-5 w-5 text-gray-500" />}
                </button>
              </div>

              {error && (
                <p className="text-red-600 text-sm bg-red-50 p-2 rounded border border-red-200" aria-live="polite">
                  {error}
                </p>
              )}

              <Button
                type="submit"
                disabled={loading}
                className="w-full bg-gradient-to-r from-teal-500 to-pink-500 hover:from-teal-600 hover:to-pink-600 text-white font-semibold py-2 px-4 rounded-lg shadow-lg transition-all duration-200"
              >
                {loading ? "Swimming in..." : "Dive In"}
              </Button>

              <p className="text-center text-sm text-teal-600 mt-4">
                Don&apos;t have an account?{" "}
                <Link to="/register" className="text-pink-600 hover:text-pink-700 font-semibold underline">
                  Adopt your first axolotl
                </Link>
              </p>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}




