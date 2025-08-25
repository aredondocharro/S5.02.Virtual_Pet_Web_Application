import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../components/ui/select";
import { ArrowLeft, Heart } from "lucide-react";
import { apiPost } from "../api/client";

type PetColor = "pink" | "black" | "white" | "orange";

const axolotlColors: Record<PetColor, string> = {
  pink: "/pictures/pink-baby.png",
  black: "/pictures/black-baby.png",
  white: "/pictures/white-baby.png",
  orange: "/pictures/orange-baby.png",
};

export default function CreatePet() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [color, setColor] = useState<PetColor | "">("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function create() {
    if (!name || !color) return;
    setIsLoading(true);
    setError(null);

    try {
      const pet = await apiPost("/api/pets", { name, color } as { name: string; color: PetColor });
      navigate(`/app/pets/${pet.id}`);
    } catch (e: any) {
      console.error(e);
      setError(e?.message ?? "Could not create your axolotl. Please try again.");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div
      className="min-h-screen grid place-items-center bg-center bg-cover md:bg-fixed relative p-6"
      style={{ backgroundImage: "url('/backgrounds/create-pet-bg.png')" }}
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
      <Link
        to="/app"
        className="absolute top-4 left-4 z-10 bg-white/80 backdrop-blur-sm rounded-full p-2 border-2 border-pink-300 hover:bg-white/90 transition-colors"
      >
        <ArrowLeft className="w-5 h-5 text-teal-700" />
      </Link>

      <Card className="w-full max-w-md bg-white/90 backdrop-blur-sm border-2 border-pink-300 shadow-2xl">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-teal-700">Create Your Axolotl</CardTitle>
          <CardDescription className="text-teal-600">
            Give your new aquatic friend a name and choose their color
          </CardDescription>
        </CardHeader>

        <CardContent className="space-y-6">
          <div>
            <Label htmlFor="name" className="text-teal-700 font-medium">
              Axolotl Name
            </Label>
            <Input
              id="name"
              placeholder="Enter a cute name..."
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="border-teal-200 focus:border-pink-400 bg-white/80"
              disabled={isLoading}
            />
          </div>

          <div>
            <Label htmlFor="color" className="text-teal-700 font-medium">
              Color
            </Label>
            <Select value={color} onValueChange={(v) => setColor(v as PetColor)}>
              <SelectTrigger className="border-teal-200 focus:border-pink-400 bg-white/80" />
              <SelectContent className="bg-white/95 backdrop-blur-sm">
                <SelectItem value="pink">Pink</SelectItem>
                <SelectItem value="black">Black</SelectItem>
                <SelectItem value="white">White</SelectItem>
                <SelectItem value="orange">Orange</SelectItem>
              </SelectContent>
              <SelectValue placeholder="Choose a color..." />
            </Select>
          </div>

          {color && (
            <div className="flex justify-center">
              <div className="relative">
                <img
                  src={axolotlColors[color as PetColor]}
                  alt={`${color} axolotl baby`}
                  className="w-32 h-32 object-contain rounded-lg"
                />
                <div className="absolute -top-2 -right-2 bg-pink-500 text-white text-xs px-2 py-1 rounded-full animate-pulse flex items-center gap-1 shadow-lg">
                  Nice to meet you!
                  <Heart className="w-3 h-3" />
                </div>
              </div>
            </div>
          )}

          {error && (
            <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded p-2">{error}</p>
          )}

          <Button
            onClick={create}
            disabled={!name || !color || isLoading}
            className="w-full bg-gradient-to-r from-teal-500 to-pink-500 hover:from-teal-600 hover:to-pink-600 disabled:opacity-50 text-white font-medium shadow-lg"
          >
            {isLoading ? "Creating your friend..." : "Create Axolotl"}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}



