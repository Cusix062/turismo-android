const express = require("express");
const cors = require("cors");
const path = require("path");

const app = express();
const PORT = 3000;

app.use(cors());
app.use(express.json());
app.use("/imagenes", express.static(path.join(__dirname, "public")));

// ---- DATOS DE TURISMO ----
const lugares = [
  {
    id: 1,
    nombre: "Playa del Carmen",
    descripcion: "Hermosa playa con aguas cristalinas y arena blanca, ideal para relajarse.",
    categoria: "Playa",
    lat: 20.6296,
    lng: -87.0789,
    imagen: null,
    popularidad: 95,
    fechaCreacion: "2024-01-15",
    direccion: "Playa del Carmen, Quintana Roo",
    horario: "Abierto todo el dia",
  },
  {
    id: 2,
    nombre: "Chichen Itza",
    descripcion: "Una de las 7 maravillas del mundo, zona arqueologica maya impresionante.",
    categoria: "Monumento",
    lat: 20.6843,
    lng: -88.5678,
    imagen: null,
    popularidad: 98,
    fechaCreacion: "2024-01-10",
    direccion: "Tinun, Yucatan",
    horario: "8:00 AM - 5:00 PM",
  },
  {
    id: 3,
    nombre: "Cenote Dos Ojos",
    descripcion: "Cenote espectacular para buceo y snorkel con aguas cristalinas.",
    categoria: "Playa",
    lat: 20.3295,
    lng: -87.3665,
    imagen: null,
    popularidad: 88,
    fechaCreacion: "2024-02-20",
    direccion: "Tulum, Quintana Roo",
    horario: "8:00 AM - 5:00 PM",
  },
  {
    id: 4,
    nombre: "Museo de Antropologia",
    descripcion: "El museo mas importante de Mexico con colecciones de culturas prehisp\u00e1nicas.",
    categoria: "Museo",
    lat: 19.4260,
    lng: -99.1863,
    imagen: null,
    popularidad: 90,
    fechaCreacion: "2024-01-05",
    direccion: "Paseo de la Reforma, CDMX",
    horario: "9:00 AM - 7:00 PM",
  },
  {
    id: 5,
    nombre: "Parque Nacional Cumbres del Ajusco",
    descripcion: "Area natural perfecta para senderismo y camping con vistas increibles.",
    categoria: "Parque",
    lat: 19.2356,
    lng: -99.2731,
    imagen: null,
    popularidad: 72,
    fechaCreacion: "2024-03-01",
    direccion: "Tlalpan, CDMX",
    horario: "6:00 AM - 6:00 PM",
  },
  {
    id: 6,
    nombre: "Restaurante La Casa del Pastor",
    descripcion: "Los mejores tacos al pastor de la ciudad con recetas tradicionales.",
    categoria: "Restaurante",
    lat: 19.4326,
    lng: -99.1420,
    imagen: null,
    popularidad: 85,
    fechaCreacion: "2024-02-15",
    direccion: "Centro Historico, CDMX",
    horario: "10:00 AM - 11:00 PM",
  },
  {
    id: 7,
    nombre: "Mirador de la Cruz",
    descripcion: "Vista panoramica de 360 grados de toda la ciudad, imperdible al atardecer.",
    categoria: "Mirador",
    lat: 19.4350,
    lng: -99.1370,
    imagen: null,
    popularidad: 78,
    fechaCreacion: "2024-04-10",
    direccion: "Col. Del Valle, CDMX",
    horario: "24 horas",
  },
  {
    id: 8,
    nombre: "Centro Historico de Oaxaca",
    descripcion: "Arquitectura colonial, mercados tradicionales y gastronomia unica.",
    categoria: "Centro Historico",
    lat: 17.0594,
    lng: -96.7215,
    imagen: null,
    popularidad: 82,
    fechaCreacion: "2024-01-20",
    direccion: "Centro, Oaxaca de Juarez",
    horario: "Abierto todo el dia",
  },
  {
    id: 9,
    nombre: "Xcaret",
    descripcion: "Parque eco-arqueologico con rios subterraneos, fauna y cultura mexicana.",
    categoria: "Parque",
    lat: 20.5760,
    lng: -87.1190,
    imagen: null,
    popularidad: 93,
    fechaCreacion: "2024-03-15",
    direccion: "Playa del Carmen, Quintana Roo",
    horario: "8:30 AM - 9:30 PM",
  },
  {
    id: 10,
    nombre: "Teotihuacan",
    descripcion: "Pir\u00e1mides del Sol y la Luna, patrimonio de la humanidad por la UNESCO.",
    categoria: "Monumento",
    lat: 19.6925,
    lng: -98.8437,
    imagen: null,
    popularidad: 96,
    fechaCreacion: "2024-01-08",
    direccion: "Teotihuacan, Estado de Mexico",
    horario: "9:00 AM - 6:00 PM",
  },
  {
    id: 11,
    nombre: "Coyoacan - Mercado",
    descripcion: "Barrio magico con calles empedradas, mercados y la casa de Frida Kahlo.",
    categoria: "Centro Historico",
    lat: 19.3467,
    lng: -99.1617,
    imagen: null,
    popularidad: 80,
    fechaCreacion: "2024-04-01",
    direccion: "Coyoacan, CDMX",
    horario: "10:00 AM - 8:00 PM",
  },
  {
    id: 12,
    nombre: "Restaurante Pujol",
    descripcion: "Alta cocina mexicana con estrella Michelin, experiencia gastronomica unica.",
    categoria: "Restaurante",
    lat: 19.4370,
    lng: -99.1910,
    imagen: null,
    popularidad: 91,
    fechaCreacion: "2024-05-01",
    direccion: "Polanco, CDMX",
    horario: "1:00 PM - 10:00 PM",
  },
  {
    id: 13,
    nombre: "Isla Mujeres",
    descripcion: "Isla caribena con playas paradis\u00edacas, ideal para snorkel y descanso.",
    categoria: "Playa",
    lat: 21.2310,
    lng: -86.7320,
    imagen: null,
    popularidad: 89,
    fechaCreacion: "2024-04-20",
    direccion: "Isla Mujeres, Quintana Roo",
    horario: "Abierto todo el dia",
  },
  {
    id: 14,
    nombre: "Museo Soumaya",
    descripcion: "Museo ic\u00f3nico con arquitectura vanguardista y colecci\u00f3n de arte mundial.",
    categoria: "Museo",
    lat: 19.4400,
    lng: -99.2040,
    imagen: null,
    popularidad: 84,
    fechaCreacion: "2024-03-25",
    direccion: "Polanco, CDMX",
    horario: "10:30 AM - 6:30 PM",
  },
  {
    id: 15,
    nombre: "Bosque de Chapultepec",
    descripcion: "El parque urbano mas grande de Latinoamerica con lagos y museos.",
    categoria: "Parque",
    lat: 19.4200,
    lng: -99.1860,
    imagen: null,
    popularidad: 87,
    fechaCreacion: "2024-02-10",
    direccion: "Chapultepec, CDMX",
    horario: "5:00 AM - 12:00 AM",
  },
];

const usuarios = [
  { id: 1, email: "demo@turismo.com", nombre: "Usuario Demo" },
];

let favoritos = [];

// ---- ENDPOINTS ----

// Obtener todos los lugares
app.get("/api/lugares", (req, res) => {
  res.json({ data: lugares });
});

// Obtener lugar por ID
app.get("/api/lugares/:id", (req, res) => {
  const lugar = lugares.find((l) => l.id === parseInt(req.params.id));
  if (lugar) {
    res.json({ data: lugar });
  } else {
    res.status(404).json({ error: "Lugar no encontrado" });
  }
});

// Buscar lugares
app.get("/api/lugares/search", (req, res) => {
  const q = (req.query.q || "").toLowerCase();
  const resultados = lugares.filter(
    (l) =>
      l.nombre.toLowerCase().includes(q) ||
      l.categoria.toLowerCase().includes(q) ||
      l.descripcion.toLowerCase().includes(q),
  );
  res.json({
    data: resultados.map((l) => ({
      id: l.id,
      nombre: l.nombre,
      categoria: l.categoria,
    })),
  });
});

// Lugares populares (ordenados por popularidad)
app.get("/api/lugares/populares", (req, res) => {
  const populares = [...lugares]
    .sort((a, b) => (b.popularidad || 0) - (a.popularidad || 0))
    .slice(0, 5);
  res.json({ data: populares });
});

// Lugares nuevos (ordenados por fecha)
app.get("/api/lugares/nuevos", (req, res) => {
  const nuevos = [...lugares]
    .sort(
      (a, b) =>
        new Date(b.fechaCreacion || 0) - new Date(a.fechaCreacion || 0),
    )
    .slice(0, 5);
  res.json({ data: nuevos });
});

// Lugares por categor\u00eda
app.get("/api/lugares/categoria/:categoria", (req, res) => {
  const cat = req.params.categoria;
  const filtrados = lugares.filter(
    (l) => l.categoria.toLowerCase() === cat.toLowerCase(),
  );
  res.json({ data: filtrados });
});

// Obtener usuario
app.get("/api/usuarios", (req, res) => {
  res.json({ data: usuarios });
});

// Obtener favoritos de un usuario
app.get("/api/usuarios/:id/favoritos", (req, res) => {
  const userId = parseInt(req.params.id);
  const favs = favoritos
    .filter((f) => f.usuarioId === userId)
    .map((f) => ({
      lugarId: f.lugarId,
      lugar: lugares.find((l) => l.id === f.lugarId) || null,
    }));
  res.json({ data: favs });
});

// Agregar favorito
app.post("/api/usuarios/:usuarioId/favoritos", (req, res) => {
  const userId = parseInt(req.params.usuarioId);
  const { lugarId } = req.body;

  const existe = favoritos.find(
    (f) => f.usuarioId === userId && f.lugarId === lugarId,
  );
  if (existe) {
    return res.status(400).json({ error: "Ya es favorito" });
  }

  favoritos.push({ usuarioId: userId, lugarId });
  res.json({ data: { usuarioId: userId, lugarId } });
});

// Eliminar favorito
app.delete("/api/usuarios/:usuarioId/favoritos/:lugarId", (req, res) => {
  const userId = parseInt(req.params.usuarioId);
  const lugarId = parseInt(req.params.lugarId);

  favoritos = favoritos.filter(
    (f) => !(f.usuarioId === userId && f.lugarId === lugarId),
  );
  res.json({ data: { usuarioId: userId, lugarId } });
});

// Iniciar servidor
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Backend Turismo corriendo en http://0.0.0.0:${PORT}`);
  console.log(`Accede desde tu celular en: http://TU_IP:${PORT}`);
});
