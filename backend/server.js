const express = require("express");
const cors = require("cors");
const path = require("path");
const fs = require("fs");
const jwt = require("jsonwebtoken");
const bcrypt = require("bcryptjs");
const multer = require("multer");

const app = express();
const PORT = 3000;
const JWT_SECRET = "turismo-app-secret-key-2026";

// Multer config
const uploadsDir = path.join(__dirname, "uploads");
if (!fs.existsSync(uploadsDir)) fs.mkdirSync(uploadsDir, { recursive: true });
const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, uploadsDir),
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname);
    cb(null, Date.now() + "-" + Math.round(Math.random() * 1e9) + ext);
  },
});
const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (req, file, cb) => {
    const allowed = /\.(jpg|jpeg|png|gif|webp)$/i;
    if (allowed.test(path.extname(file.originalname))) cb(null, true);
    else cb(new Error("Solo imagenes (jpg, png, gif, webp)"));
  },
});

app.use(cors());
app.use(express.json());
app.use("/uploads", express.static(uploadsDir));

// ---- DATOS ----
const lugares = [
  { id: 1, nombre: "Playa del Carmen", descripcion: "Hermosa playa con aguas cristalinas y arena blanca, ideal para relajarse.", categoria: "Playa", lat: 20.6296, lng: -87.0789, imagen: null, popularidad: 95, visitas: 1520, fechaCreacion: "2024-01-15", direccion: "Playa del Carmen, Quintana Roo", horario: "Abierto todo el dia", creadoPor: null },
  { id: 2, nombre: "Chichen Itza", descripcion: "Una de las 7 maravillas del mundo, zona arqueologica maya impresionante.", categoria: "Monumento", lat: 20.6843, lng: -88.5678, imagen: null, popularidad: 98, visitas: 2300, fechaCreacion: "2024-01-10", direccion: "Tinun, Yucatan", horario: "8:00 AM - 5:00 PM", creadoPor: null },
  { id: 3, nombre: "Cenote Dos Ojos", descripcion: "Cenote espectacular para buceo y snorkel con aguas cristalinas.", categoria: "Playa", lat: 20.3295, lng: -87.3665, imagen: null, popularidad: 88, visitas: 980, fechaCreacion: "2024-02-20", direccion: "Tulum, Quintana Roo", horario: "8:00 AM - 5:00 PM", creadoPor: null },
  { id: 4, nombre: "Museo de Antropologia", descripcion: "El museo mas importante de Mexico con colecciones de culturas prehispanicas.", categoria: "Museo", lat: 19.4260, lng: -99.1863, imagen: null, popularidad: 90, visitas: 1870, fechaCreacion: "2024-01-05", direccion: "Paseo de la Reforma, CDMX", horario: "9:00 AM - 7:00 PM", creadoPor: null },
  { id: 5, nombre: "Parque Nacional Cumbres del Ajusco", descripcion: "Area natural perfecta para senderismo y camping con vistas increibles.", categoria: "Parque", lat: 19.2356, lng: -99.2731, imagen: null, popularidad: 72, visitas: 540, fechaCreacion: "2024-03-01", direccion: "Tlalpan, CDMX", horario: "6:00 AM - 6:00 PM", creadoPor: null },
  { id: 6, nombre: "Restaurante La Casa del Pastor", descripcion: "Los mejores tacos al pastor de la ciudad con recetas tradicionales.", categoria: "Restaurante", lat: 19.4326, lng: -99.1420, imagen: null, popularidad: 85, visitas: 2100, fechaCreacion: "2024-02-15", direccion: "Centro Historico, CDMX", horario: "10:00 AM - 11:00 PM", creadoPor: null },
  { id: 7, nombre: "Mirador de la Cruz", descripcion: "Vista panoramica de 360 grados de toda la ciudad, imperdible al atardecer.", categoria: "Mirador", lat: 19.4350, lng: -99.1370, imagen: null, popularidad: 78, visitas: 760, fechaCreacion: "2024-04-10", direccion: "Col. Del Valle, CDMX", horario: "24 horas", creadoPor: null },
  { id: 8, nombre: "Centro Historico de Oaxaca", descripcion: "Arquitectura colonial, mercados tradicionales y gastronomia unica.", categoria: "Centro Historico", lat: 17.0594, lng: -96.7215, imagen: null, popularidad: 82, visitas: 1340, fechaCreacion: "2024-01-20", direccion: "Centro, Oaxaca de Juarez", horario: "Abierto todo el dia", creadoPor: null },
  { id: 9, nombre: "Xcaret", descripcion: "Parque eco-arqueologico con rios subterraneos, fauna y cultura mexicana.", categoria: "Parque", lat: 20.5760, lng: -87.1190, imagen: null, popularidad: 93, visitas: 2900, fechaCreacion: "2024-03-15", direccion: "Playa del Carmen, Quintana Roo", horario: "8:30 AM - 9:30 PM", creadoPor: null },
  { id: 10, nombre: "Teotihuacan", descripcion: "Piramides del Sol y la Luna, patrimonio de la humanidad por la UNESCO.", categoria: "Monumento", lat: 19.6925, lng: -98.8437, imagen: null, popularidad: 96, visitas: 3100, fechaCreacion: "2024-01-08", direccion: "Teotihuacan, Estado de Mexico", horario: "9:00 AM - 6:00 PM", creadoPor: null },
  { id: 11, nombre: "Coyoacan - Mercado", descripcion: "Barrio magico con calles empedradas, mercados y la casa de Frida Kahlo.", categoria: "Centro Historico", lat: 19.3467, lng: -99.1617, imagen: null, popularidad: 80, visitas: 890, fechaCreacion: "2024-04-01", direccion: "Coyoacan, CDMX", horario: "10:00 AM - 8:00 PM", creadoPor: null },
  { id: 12, nombre: "Restaurante Pujol", descripcion: "Alta cocina mexicana con estrella Michelin, experiencia gastronomica unica.", categoria: "Restaurante", lat: 19.4370, lng: -99.1910, imagen: null, popularidad: 91, visitas: 1750, fechaCreacion: "2024-05-01", direccion: "Polanco, CDMX", horario: "1:00 PM - 10:00 PM", creadoPor: null },
  { id: 13, nombre: "Isla Mujeres", descripcion: "Isla caribena con playas paradisiacas, ideal para snorkel y descanso.", categoria: "Playa", lat: 21.2310, lng: -86.7320, imagen: null, popularidad: 89, visitas: 1420, fechaCreacion: "2024-04-20", direccion: "Isla Mujeres, Quintana Roo", horario: "Abierto todo el dia", creadoPor: null },
  { id: 14, nombre: "Museo Soumaya", descripcion: "Museo iconico con arquitectura vanguardista y coleccion de arte mundial.", categoria: "Museo", lat: 19.4400, lng: -99.2040, imagen: null, popularidad: 84, visitas: 1200, fechaCreacion: "2024-03-25", direccion: "Polanco, CDMX", horario: "10:30 AM - 6:30 PM", creadoPor: null },
  { id: 15, nombre: "Bosque de Chapultepec", descripcion: "El parque urbano mas grande de Latinoamerica con lagos y museos.", categoria: "Parque", lat: 19.4200, lng: -99.1860, imagen: null, popularidad: 87, visitas: 1050, fechaCreacion: "2024-02-10", direccion: "Chapultepec, CDMX", horario: "5:00 AM - 12:00 AM", creadoPor: null },
];

// In-memory stores
let usuarios = [];
let favoritos = [];
let comentarios = [];

let nextUserId = 1;
let nextComentarioId = 1;
let nextLugarId = 16;
let visitasRegistro = []; // { usuarioId, lugarId, fecha }

// ---- AUTH MIDDLEWARE ----
function autenticar(req, res, next) {
  const header = req.headers.authorization;
  if (!header || !header.startsWith("Bearer ")) {
    return res.status(401).json({ error: "Token requerido" });
  }
  try {
    const decoded = jwt.verify(header.split(" ")[1], JWT_SECRET);
    req.usuario = decoded;
    next();
  } catch {
    res.status(401).json({ error: "Token invalido" });
  }
}

// ---- AUTH ENDPOINTS ----

app.post("/api/auth/register", async (req, res) => {
  try {
    const { email, nombre, password } = req.body;
    if (!email || !nombre || !password) {
      return res.status(400).json({ error: "email, nombre y password requeridos" });
    }
    if (usuarios.find((u) => u.email === email)) {
      return res.status(400).json({ error: "El email ya esta registrado" });
    }
    const hash = await bcrypt.hash(password, 10);
    const usuario = { id: nextUserId++, email, nombre, password: hash };
    usuarios.push(usuario);

    const token = jwt.sign({ id: usuario.id, email: usuario.email, nombre: usuario.nombre }, JWT_SECRET, { expiresIn: "30d" });
    res.status(201).json({ data: { id: usuario.id, email: usuario.email, nombre: usuario.nombre, token } });
  } catch (err) {
    res.status(500).json({ error: "Error al registrar" });
  }
});

app.post("/api/auth/login", async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ error: "email y password requeridos" });
    }
    const usuario = usuarios.find((u) => u.email === email);
    if (!usuario) {
      return res.status(401).json({ error: "Credenciales invalidas" });
    }
    const valido = await bcrypt.compare(password, usuario.password);
    if (!valido) {
      return res.status(401).json({ error: "Credenciales invalidas" });
    }
    const token = jwt.sign({ id: usuario.id, email: usuario.email, nombre: usuario.nombre }, JWT_SECRET, { expiresIn: "30d" });
    res.json({ data: { id: usuario.id, email: usuario.email, nombre: usuario.nombre, token } });
  } catch {
    res.status(500).json({ error: "Error al iniciar sesion" });
  }
});

// ---- LUGARES ENDPOINTS ----

app.get("/api/lugares", (req, res) => {
  res.json({ data: lugares });
});

app.get("/api/lugares/:id", (req, res) => {
  const lugar = lugares.find((l) => l.id === parseInt(req.params.id));
  if (lugar) {
    res.json({ data: lugar });
  } else {
    res.status(404).json({ error: "Lugar no encontrado" });
  }
});

app.get("/api/lugares/search", (req, res) => {
  const q = (req.query.q || "").toLowerCase();
  const resultados = lugares.filter(
    (l) =>
      l.nombre.toLowerCase().includes(q) ||
      l.categoria.toLowerCase().includes(q) ||
      l.descripcion.toLowerCase().includes(q),
  );
  res.json({
    data: resultados.map((l) => ({ id: l.id, nombre: l.nombre, categoria: l.categoria })),
  });
});

app.get("/api/lugares/populares", (req, res) => {
  const populares = [...lugares].sort((a, b) => (b.popularidad || 0) - (a.popularidad || 0)).slice(0, 5);
  res.json({ data: populares });
});

app.get("/api/lugares/nuevos", (req, res) => {
  const nuevos = [...lugares].sort((a, b) => new Date(b.fechaCreacion || 0) - new Date(a.fechaCreacion || 0)).slice(0, 5);
  res.json({ data: nuevos });
});

app.get("/api/lugares/categoria/:categoria", (req, res) => {
  const cat = req.params.categoria;
  const filtrados = lugares.filter((l) => l.categoria.toLowerCase() === cat.toLowerCase());
  res.json({ data: filtrados });
});

// ---- LUGARES CRUD (usuarios) ----

app.post("/api/lugares", autenticar, upload.single("imagen"), (req, res) => {
  try {
    const { nombre, descripcion, categoria, lat, lng, direccion, horario } = req.body;
    if (!nombre || !descripcion || !categoria || !lat || !lng) {
      return res.status(400).json({ error: "nombre, descripcion, categoria, lat, lng requeridos" });
    }
    const nuevo = {
      id: nextLugarId++,
      nombre,
      descripcion,
      categoria,
      lat: parseFloat(lat),
      lng: parseFloat(lng),
      imagen: req.file ? "/uploads/" + req.file.filename : null,
      popularidad: 0,
      visitas: 0,
      fechaCreacion: new Date().toISOString().split("T")[0],
      direccion: direccion || null,
      horario: horario || null,
      creadoPor: req.usuario.id,
    };
    lugares.push(nuevo);
    res.status(201).json({ data: nuevo });
  } catch (err) {
    res.status(500).json({ error: "Error al crear lugar" });
  }
});

app.put("/api/lugares/:id", autenticar, upload.single("imagen"), (req, res) => {
  const lugar = lugares.find((l) => l.id === parseInt(req.params.id));
  if (!lugar) return res.status(404).json({ error: "Lugar no encontrado" });
  // Only allow update by creator (seed places have creadoPor = null, only admin can edit them)
  if (lugar.creadoPor !== null && lugar.creadoPor !== req.usuario.id) {
    return res.status(403).json({ error: "No tienes permiso para editar este lugar" });
  }
  const { nombre, descripcion, categoria, lat, lng, direccion, horario } = req.body;
  if (nombre) lugar.nombre = nombre;
  if (descripcion) lugar.descripcion = descripcion;
  if (categoria) lugar.categoria = categoria;
  if (lat) lugar.lat = parseFloat(lat);
  if (lng) lugar.lng = parseFloat(lng);
  if (direccion) lugar.direccion = direccion;
  if (horario) lugar.horario = horario;
  if (req.file) lugar.imagen = "/uploads/" + req.file.filename;
  res.json({ data: lugar });
});

app.delete("/api/lugares/:id", autenticar, (req, res) => {
  const idx = lugares.findIndex((l) => l.id === parseInt(req.params.id));
  if (idx === -1) return res.status(404).json({ error: "Lugar no encontrado" });
  const lugar = lugares[idx];
  if (lugar.creadoPor !== null && lugar.creadoPor !== req.usuario.id) {
    return res.status(403).json({ error: "No tienes permiso para eliminar este lugar" });
  }
  lugares.splice(idx, 1);
  res.json({ data: { id: lugar.id } });
});

// ---- LUGARES CERCANOS ----

app.get("/api/lugares/cercanos", (req, res) => {
  const { lat, lng, radio } = req.query;
  if (!lat || !lng) return res.status(400).json({ error: "lat y lng requeridos" });
  const latNum = parseFloat(lat);
  const lngNum = parseFloat(lng);
  const radioKm = parseFloat(radio) || 10;

  const cercanos = lugares.filter((l) => {
    if (l.lat == null || l.lng == null) return false;
    const d = distanciaKm(latNum, lngNum, l.lat, l.lng);
    return d <= radioKm;
  }).map((l) => ({
    ...l,
    distanciaKm: Math.round(distanciaKm(latNum, lngNum, l.lat, l.lng) * 100) / 100,
  })).sort((a, b) => a.distanciaKm - b.distanciaKm);

  res.json({ data: cercanos });
});

// ---- LUGARES RECOMENDADOS (populares + cercanos + bien valorados) ----

app.get("/api/lugares/recomendados", (req, res) => {
  const { lat, lng, limite } = req.query;
  let resultado = [...lugares];

  if (lat && lng) {
    const latNum = parseFloat(lat);
    const lngNum = parseFloat(lng);
    resultado = resultado.filter((l) => l.lat != null && l.lng != null);
    // Calculate composite score: popularidad * 0.4 + visitas * 0.3 - distancia * 0.3
    resultado = resultado.map((l) => {
      const d = distanciaKm(latNum, lngNum, l.lat, l.lng);
      const score = (l.popularidad || 0) * 0.4 + Math.min((l.visitas || 0) / 100, 50) * 0.3 + Math.max(50 - d * 2, 0) * 0.3;
      return { ...l, distanciaKm: Math.round(d * 100) / 100, score: Math.round(score * 100) / 100 };
    }).sort((a, b) => b.score - a.score);
  } else {
    resultado = resultado.sort((a, b) => (b.popularidad || 0) - (a.popularidad || 0));
  }

  const lim = parseInt(limite) || 10;
  res.json({ data: resultado.slice(0, lim) });
});

// ---- VISITAS ----

app.post("/api/lugares/:id/visitar", autenticar, (req, res) => {
  const lugarId = parseInt(req.params.id);
  const lugar = lugares.find((l) => l.id === lugarId);
  if (!lugar) return res.status(404).json({ error: "Lugar no encontrado" });
  lugar.visitas = (lugar.visitas || 0) + 1;
  visitasRegistro.push({ usuarioId: req.usuario.id, lugarId, fecha: new Date().toISOString() });
  res.json({ data: { visitas: lugar.visitas } });
});

// ---- COMENTARIOS ----

app.get("/api/lugares/:id/comentarios", (req, res) => {
  const lugarId = parseInt(req.params.id);
  const comentariosLugar = comentarios
    .filter((c) => c.lugarId === lugarId)
    .sort((a, b) => new Date(b.fecha) - new Date(a.fecha));
  res.json({ data: comentariosLugar });
});

app.post("/api/lugares/:id/comentarios", autenticar, (req, res) => {
  const lugarId = parseInt(req.params.id);
  const { texto, calificacion } = req.body;
  if (!texto) {
    return res.status(400).json({ error: "El texto es requerido" });
  }
  const nuevo = {
    id: nextComentarioId++,
    lugarId,
    usuarioId: req.usuario.id,
    usuarioNombre: req.usuario.nombre,
    texto,
    calificacion: calificacion || 5,
    fecha: new Date().toISOString(),
  };
  comentarios.push(nuevo);
  res.status(201).json({ data: nuevo });
});

// ---- FAVORITOS (autenticados) ----

app.get("/api/favoritos", autenticar, (req, res) => {
  const favs = favoritos
    .filter((f) => f.usuarioId === req.usuario.id)
    .map((f) => ({
      lugarId: f.lugarId,
      lugar: lugares.find((l) => l.id === f.lugarId) || null,
    }));
  res.json({ data: favs });
});

app.post("/api/favoritos", autenticar, (req, res) => {
  const { lugarId } = req.body;
  const existe = favoritos.find((f) => f.usuarioId === req.usuario.id && f.lugarId === lugarId);
  if (existe) {
    return res.status(400).json({ error: "Ya es favorito" });
  }
  favoritos.push({ usuarioId: req.usuario.id, lugarId });
  res.json({ data: { usuarioId: req.usuario.id, lugarId } });
});

app.delete("/api/favoritos/:lugarId", autenticar, (req, res) => {
  const lugarId = parseInt(req.params.lugarId);
  favoritos = favoritos.filter((f) => !(f.usuarioId === req.usuario.id && f.lugarId === lugarId));
  res.json({ data: { usuarioId: req.usuario.id, lugarId } });
});

// ---- RUTAS (proxy a OSRM) ----

app.get("/api/rutas", async (req, res) => {
  const { origen, destino } = req.query;
  if (!origen || !destino) {
    return res.status(400).json({ error: "origen y destino requeridos (formato: lat,lng)" });
  }
  try {
    const resp = await fetch(
      `https://router.project-osrm.org/route/v1/driving/${origen};${destino}?overview=full&geometries=geojson&steps=true`,
      { timeout: 10000 },
    );
    const data = await resp.json();
    res.json(data);
  } catch {
    res.status(502).json({ error: "No se pudo calcular la ruta" });
  }
});

// ---- HELPERS ----

function distanciaKm(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

// Iniciar servidor
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Backend Turismo corriendo en http://0.0.0.0:${PORT}`);
});
