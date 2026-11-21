(function () {
  "use strict";

  // =========================================================================
  // 1. CONSTANTS & ECONOMY TABLES (1:1 parity with TycoonModels.kt)
  // =========================================================================
  const RESOURCES = {
    wood:    { name: "Wood",       baseValue: 6,   color: 0xb45309, css: "#fbbf24" },
    stone:   { name: "Stone",      baseValue: 10,  color: 0x94a3b8, css: "#cbd5e1" },
    coal:    { name: "Coal",       baseValue: 16,  color: 0x334155, css: "#94a3b8" },
    copper:  { name: "Copper",     baseValue: 26,  color: 0xea580c, css: "#fb923c" },
    iron:    { name: "Iron",       baseValue: 38,  color: 0xcbd5e1, css: "#e2e8f0" },
    gold:    { name: "Gold",       baseValue: 65,  color: 0xfbbf24, css: "#fde047" },
    crystal: { name: "Crystal",    baseValue: 110, color: 0x22d3ee, css: "#67e8f9" },
    rare:    { name: "Rare Relic", baseValue: 220, color: 0xe879f9, css: "#f0abfc" }
  };

  const BIOMES = [
    { id: "forest",    name: "Green Forest",    cx: 0,   cz: 0,   unlockCost: 0,     reqAxe: 1, reqPick: 1, reqBag: 1, sky: 0x6ec3eb, fog: 0x9bd7f2, ground: 0x3b8c47, accent: 0x10b981, gate: null },
    { id: "mountains", name: "Rocky Mountains", cx: 0,   cz: -34, unlockCost: 150,   reqAxe: 2, reqPick: 2, reqBag: 1, sky: 0x7c96ad, fog: 0x9ab0c4, ground: 0x5c6672, accent: 0x94a3b8, gate: { x: 0, z: -17, dir: "z" } },
    { id: "desert",    name: "Golden Desert",   cx: 34,  cz: 0,   unlockCost: 650,   reqAxe: 1, reqPick: 3, reqBag: 1, sky: 0xf3be78, fog: 0xf7cf97, ground: 0xd9a14a, accent: 0xf59e0b, gate: { x: 17, z: 0, dir: "x" } },
    { id: "frozen",    name: "Frozen Lands",    cx: -34, cz: 0,   unlockCost: 2000,  reqAxe: 1, reqPick: 4, reqBag: 3, sky: 0x95cde8, fog: 0xbfe2f4, ground: 0xcbe7f5, accent: 0x38bdf8, gate: { x: -17, z: 0, dir: "x" } },
    { id: "volcanic",  name: "Volcanic Region", cx: 0,   cz: 34,  unlockCost: 6500,  reqAxe: 1, reqPick: 5, reqBag: 4, sky: 0x4d201d, fog: 0x6b2c25, ground: 0x2d2726, accent: 0xef4444, gate: { x: 0, z: 17, dir: "z" } },
    { id: "jungle",    name: "Ancient Jungle",  cx: 34,  cz: -34, unlockCost: 18000, reqAxe: 6, reqPick: 6, reqBag: 1, sky: 0x44a399, fog: 0x6ac2b8, ground: 0x1e5c4a, accent: 0x14b8a6, gate: { x: 34, z: -17, dir: "z" } }
  ];

  const BUILDING_DEFS = [
    { id: "sawmill",         title: "Timber Sawmill",   biome: "forest",    x: 7.5,   z: 6.5,   baseCost: 120,   growth: 2.25, passive: 4 },
    { id: "warehouse",       title: "Grand Warehouse",  biome: "forest",    x: 7.5,   z: -4.5,  baseCost: 180,   growth: 2.3,  passive: 2 },
    { id: "miner_forest",    title: "Lumber Bot",       biome: "forest",    x: -8.0,  z: -6.0,  baseCost: 90,    growth: 2.15, passive: 3 },
    { id: "smelter",         title: "Ore Smelter",      biome: "mountains", x: 7.5,   z: -29.0, baseCost: 320,   growth: 2.3,  passive: 10 },
    { id: "miner_mountains", title: "Drill Rig",        biome: "mountains", x: -7.5,  z: -29.0, baseCost: 350,   growth: 2.15, passive: 12 },
    { id: "miner_desert",    title: "Solar Extractor",  biome: "desert",    x: 34.0,  z: 7.5,   baseCost: 950,   growth: 2.15, passive: 30 },
    { id: "miner_frozen",    title: "Cryo Harvester",   biome: "frozen",    x: -34.0, z: 7.5,   baseCost: 2800,  growth: 2.15, passive: 75 },
    { id: "miner_volcanic",  title: "Magma Extractor",  biome: "volcanic",  x: 7.5,   z: 34.0,  baseCost: 8000,  growth: 2.15, passive: 190 },
    { id: "miner_jungle",    title: "Relic Synth",      biome: "jungle",    x: 34.0,  z: -26.5, baseCost: 22000, growth: 2.15, passive: 450 }
  ];

  const MACHINE_DEFS = [
    { id: "wood_cutter",        biome: "forest",    x: 11.6,  z: 4.8,   rotY: -Math.PI / 2, color: 0xd97706 },
    { id: "stone_crusher",      biome: "forest",    x: -11.4, z: -5.2,  rotY: Math.PI / 2,  color: 0x94a3b8 },
    { id: "conveyor_belt",      biome: "forest",    x: 9.6,   z: 0.6,   rotY: 0,            color: 0x38bdf8 },
    { id: "sawmill_machine",    biome: "forest",    x: 11.6,  z: 9.2,   rotY: -Math.PI / 2, color: 0xf59e0b },
    { id: "auto_miner_machine", biome: "mountains", x: -11.2, z: -32.0, rotY: 0.4,          color: 0x10b981 },
    { id: "ore_processor",      biome: "mountains", x: 11.4,  z: -33.2, rotY: -Math.PI / 2, color: 0xfb923c },
    { id: "smelting_furnace",   biome: "mountains", x: 11.4,  z: -25.4, rotY: -Math.PI / 2, color: 0xef4444 },
    { id: "gold_refinery",      biome: "desert",    x: 27.6,  z: -5.4,  rotY: 0.3,          color: 0xfde047 },
    { id: "crystal_processor",  biome: "frozen",    x: -27.6, z: -5.4,  rotY: -0.3,         color: 0x22d3ee },
    { id: "industrial_factory", biome: "volcanic",  x: 11.4,  z: 28.5,  rotY: -Math.PI / 2, color: 0xe879f9 }
  ];

  const BAG_CAPS = [12, 22, 36, 56, 84, 126, 184, 260];
  const MOVE_SPEEDS = [6.4, 7.4, 8.5, 9.7, 11.0, 12.4, 13.9, 15.6];
  const GATHER_RATES = [1.0, 1.25, 1.55, 1.9, 2.3, 2.75, 3.25, 3.85];
  const TOOL_DAMAGES = [1, 2, 3, 5, 7, 9, 12, 16];

  const TIER_COLORS = [
    0x92400e, // 1: Wooden
    0x94a3b8, // 2: Stone
    0xea580c, // 3: Copper
    0xcbd5e1, // 4: Iron
    0x64748b, // 5: Steel
    0xfbbf24, // 6: Gold
    0x22d3ee, // 7: Diamond
    0xe879f9  // 8: Legendary
  ];

  // =========================================================================
  // 2. GAME STATE & MENU MODE
  // =========================================================================
  let state = {
    coins: 0,
    lifetimeEarned: 0,
    xp: 0,
    inventory: {},
    gatheredTotals: {},
    axeLevel: 1,
    pickaxeLevel: 1,
    backpackLevel: 1,
    moveSpeedLevel: 1,
    gatherSpeedLevel: 1,
    resourceUpgradeLevels: {},
    buildingLevels: {},
    buildingWorkers: {},
    buildingLines: {},
    machineLevels: {},
    machineActive: {},
    warehouseStock: {},
    fleetLevels: {},
    unlockedBiomes: ["forest"],
    claimedQuestIds: [],
    uniqueItems: [],
    isMuted: false,
    sfxVolume: 0.85,
    musicVolume: 0.45,
    graphicsQuality: 1,
    hapticsEnabled: true
  };

  let isMenuMode = true;
  let stateDirty = false;
  let lastSyncTime = 0;

  function hasUniqueItem(id) {
    return Array.isArray(state.uniqueItems) && state.uniqueItems.indexOf(id) !== -1;
  }

  function getResUpgradeLevel(id) {
    if (!state.resourceUpgradeLevels) return 1;
    return Math.min(8, Math.max(1, state.resourceUpgradeLevels[id] || 1));
  }

  function getBuildingWorkers(id) {
    return (state.buildingWorkers && state.buildingWorkers[id]) ? state.buildingWorkers[id] : 0;
  }

  function getBuildingLines(id) {
    return (state.buildingLines && state.buildingLines[id]) ? state.buildingLines[id] : 0;
  }

  function getMachineLevel(id) {
    return (state.machineLevels && state.machineLevels[id]) ? state.machineLevels[id] : 0;
  }

  function isMachineRunning(id) {
    if (getMachineLevel(id) <= 0) return false;
    if (!state.machineActive || state.machineActive[id] === undefined) return true;
    return Boolean(state.machineActive[id]);
  }

  function getFleetLevel(id) {
    return (state.fleetLevels && state.fleetLevels[id]) ? state.fleetLevels[id] : 0;
  }

  function meetsBiomeGear(biome) {
    return state.axeLevel >= (biome.reqAxe || 1) &&
      state.pickaxeLevel >= (biome.reqPick || 1) &&
      state.backpackLevel >= (biome.reqBag || 1);
  }

  function getMaxBag() {
    const base = BAG_CAPS[Math.min(Math.max(state.backpackLevel, 1), 8) - 1];
    const storageLvl = getResUpgradeLevel("storageCapacity");
    const storageFlat = (storageLvl - 1) * 6;
    const storageMult = 1 + (storageLvl - 1) * 0.1;
    const wh = state.buildingLevels.warehouse || 0;
    const whLines = getBuildingLines("warehouse");
    const whBonus = wh * (8 + whLines * 4);
    const lantern = hasUniqueItem("prism_lantern") ? 10 : 0;
    return Math.round((base + storageFlat + whBonus + lantern) * storageMult);
  }

  function getBagCount() {
    let sum = 0;
    for (const k in state.inventory) sum += state.inventory[k] || 0;
    return sum;
  }

  function getPassivePerSec() {
    const autoLvl = getResUpgradeLevel("autoProduction");
    const autoMult = 1 + (autoLvl - 1) * 0.2;
    let total = 0;
    for (const b of BUILDING_DEFS) {
      const lvl = state.buildingLevels[b.id] || 0;
      if (lvl <= 0) continue;
      const staffMult = (1 + getBuildingWorkers(b.id) * 0.35) * (1 + getBuildingLines(b.id) * 0.65);
      total += lvl * b.passive * staffMult;
    }
    total *= autoMult;
    if (hasUniqueItem("tycoon_crown")) total *= 1.35;
    total *= (1 + getFleetLevel("excavator") * 0.15);
    return total;
  }

  function getSellValue(resourceId) {
    const def = RESOURCES[resourceId];
    if (!def) return 5;
    const tradeLvl = getResUpgradeLevel("tradeValue");
    const whLvl = state.buildingLevels.warehouse || 0;
    const whLines = getBuildingLines("warehouse");
    let mult = (1.0 + (tradeLvl - 1) * 0.15) + whLvl * (0.2 + whLines * 0.08);
    if (resourceId === "wood") {
      const sawLvl = state.buildingLevels.sawmill || 0;
      const sawBoost = 1 + getBuildingWorkers("sawmill") * 0.25 + getBuildingLines("sawmill") * 0.35;
      mult += sawLvl * 0.45 * sawBoost;
      if (hasUniqueItem("timber_charm")) mult += 0.25;
    }
    if (resourceId === "stone" || resourceId === "coal" || resourceId === "copper" || resourceId === "iron" || resourceId === "gold") {
      const smLvl = state.buildingLevels.smelter || 0;
      const smBoost = 1 + getBuildingWorkers("smelter") * 0.25 + getBuildingLines("smelter") * 0.35;
      mult += smLvl * 0.45 * smBoost;
      if (hasUniqueItem("pickaxe_crest")) mult += 0.25;
    }
    if (resourceId === "crystal" && hasUniqueItem("prism_lantern")) mult += 0.25;
    if (hasUniqueItem("golden_compass")) mult += 0.15;
    if (hasUniqueItem("tycoon_crown")) mult += 0.35;
    return Math.max(1, Math.round(def.baseValue * mult));
  }

  function isBiomeUnlocked(biomeId) {
    return state.unlockedBiomes.indexOf(biomeId) !== -1;
  }

  function formatMoney(n) {
    if (n >= 1000000) return "$" + (n / 1000000).toFixed(2) + "M";
    if (n >= 10000) return "$" + (n / 1000).toFixed(1) + "K";
    return "$" + Math.floor(n).toLocaleString();
  }

  // =========================================================================
  // 3. MULTI-LAYERED WEB AUDIO STUDIO + RELAXING BACKGROUND MUSIC
  // =========================================================================
  let audioCtx = null;
  let noiseBuffer = null;
  let musicStep = 0;
  let musicTimer = 0;
  const FOREST_NOTES = [261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 392.00, 329.63];

  function ensureAudio() {
    if (state.isMuted) return null;
    if (!audioCtx) {
      const AC = window.AudioContext || window.webkitAudioContext;
      if (AC) audioCtx = new AC();
    }
    if (audioCtx && audioCtx.state === "suspended") {
      audioCtx.resume().catch(function () {});
    }
    if (audioCtx && !noiseBuffer) {
      const len = Math.floor(audioCtx.sampleRate * 0.35);
      noiseBuffer = audioCtx.createBuffer(1, len, audioCtx.sampleRate);
      const data = noiseBuffer.getChannelData(0);
      for (let i = 0; i < len; i++) data[i] = Math.random() * 2 - 1;
    }
    return audioCtx;
  }

  function getSfxGain() {
    if (state.isMuted) return 0;
    return Math.max(0, Math.min(1, typeof state.sfxVolume === "number" ? state.sfxVolume : 0.85));
  }

  function getMusicGain() {
    if (state.isMuted) return 0;
    return Math.max(0, Math.min(1, typeof state.musicVolume === "number" ? state.musicVolume : 0.45));
  }

  function playTone(freq, endFreq, dur, type, baseVol, delaySec) {
    const master = getSfxGain();
    if (master <= 0.01) return;
    const ctx = ensureAudio();
    if (!ctx) return;
    const t0 = ctx.currentTime + (delaySec || 0);
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = type || "triangle";
    osc.frequency.setValueAtTime(Math.max(25, freq), t0);
    if (endFreq && endFreq !== freq) {
      osc.frequency.exponentialRampToValueAtTime(Math.max(25, endFreq), t0 + dur);
    }
    const peak = Math.max(0.001, (baseVol || 0.12) * master);
    gain.gain.setValueAtTime(peak, t0);
    gain.gain.exponentialRampToValueAtTime(0.0008, t0 + dur);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start(t0);
    osc.stop(t0 + dur + 0.02);
  }

  function playNoiseBurst(filterFreq, qVal, dur, baseVol, delaySec, filterType) {
    const master = getSfxGain();
    if (master <= 0.01) return;
    const ctx = ensureAudio();
    if (!ctx || !noiseBuffer) return;
    const t0 = ctx.currentTime + (delaySec || 0);
    const src = ctx.createBufferSource();
    src.buffer = noiseBuffer;
    const filter = ctx.createBiquadFilter();
    filter.type = filterType || "bandpass";
    filter.frequency.setValueAtTime(filterFreq || 800, t0);
    filter.Q.setValueAtTime(qVal || 2.0, t0);
    const gain = ctx.createGain();
    const peak = Math.max(0.001, (baseVol || 0.1) * master);
    gain.gain.setValueAtTime(peak, t0);
    gain.gain.exponentialRampToValueAtTime(0.0008, t0 + dur);
    src.connect(filter);
    filter.connect(gain);
    gain.connect(ctx.destination);
    src.start(t0);
    src.stop(t0 + dur + 0.02);
  }

  function playMaterialHitSfx(kind, dmgRatio) {
    const pitchLift = 1 + dmgRatio * 0.18;
    if (kind === "wood") {
      playTone(175 * pitchLift, 78, 0.09, "triangle", 0.19, 0);
      playNoiseBurst(760 * pitchLift, 2.2, 0.06, 0.14, 0.005, "bandpass");
      playTone(880, 420, 0.035, "sawtooth", 0.05, 0.015);
    } else if (kind === "stone") {
      playTone(1180 * pitchLift, 720, 0.06, "triangle", 0.11, 0);
      playNoiseBurst(1600, 1.6, 0.08, 0.13, 0.005, "bandpass");
      playTone(145, 70, 0.08, "sine", 0.12, 0.01);
    } else if (kind === "coal") {
      playNoiseBurst(2100, 1.2, 0.075, 0.15, 0, "highpass");
      playTone(210, 95, 0.07, "square", 0.07, 0.006);
    } else if (kind === "copper") {
      playTone(720 * pitchLift, 690, 0.14, "sine", 0.11, 0);
      playTone(1080 * pitchLift, 1020, 0.11, "triangle", 0.07, 0.01);
      playNoiseBurst(1900, 3.0, 0.04, 0.08, 0, "bandpass");
    } else if (kind === "iron") {
      playTone(520 * pitchLift, 495, 0.16, "triangle", 0.14, 0);
      playTone(1360 * pitchLift, 1290, 0.12, "sine", 0.08, 0.005);
      playNoiseBurst(2600, 3.5, 0.05, 0.09, 0, "bandpass");
    } else if (kind === "gold") {
      playTone(880 * pitchLift, 870, 0.18, "sine", 0.12, 0);
      playTone(1320 * pitchLift, 1310, 0.15, "sine", 0.09, 0.015);
      playTone(1760, 1740, 0.11, "sine", 0.05, 0.03);
    } else if (kind === "crystal") {
      playTone(1320 * pitchLift, 1480, 0.18, "sine", 0.11, 0);
      playTone(1980 * pitchLift, 2120, 0.15, "sine", 0.08, 0.02);
    } else {
      playTone(440 * pitchLift, 660, 0.16, "triangle", 0.11, 0);
      playTone(1120, 1540, 0.18, "sine", 0.09, 0.02);
    }
  }

  function playMaterialBreakSfx(kind) {
    if (kind === "wood") {
      playTone(250, 95, 0.24, "sawtooth", 0.07, 0);
      playNoiseBurst(1250, 2.5, 0.06, 0.14, 0.08, "bandpass");
      playNoiseBurst(980, 2.0, 0.07, 0.13, 0.19, "bandpass");
      playTone(95, 34, 0.2, "sine", 0.2, 0.34);
    } else if (kind === "stone") {
      playTone(125, 42, 0.18, "triangle", 0.17, 0);
      playNoiseBurst(900, 1.1, 0.16, 0.16, 0.01, "lowpass");
      playNoiseBurst(2200, 3.0, 0.04, 0.09, 0.11, "bandpass");
    } else if (kind === "coal") {
      playNoiseBurst(1600, 1.0, 0.18, 0.16, 0, "bandpass");
      playTone(160, 60, 0.14, "square", 0.08, 0.02);
    } else if (kind === "copper") {
      [720, 900, 1080].forEach((f, idx) => playTone(f, f * 0.98, 0.16, "sine", 0.1, idx * 0.04));
    } else if (kind === "iron") {
      [520, 780, 1040].forEach((f, idx) => playTone(f, f * 0.97, 0.18, "triangle", 0.11, idx * 0.045));
    } else if (kind === "gold") {
      [880, 1108, 1318, 1760].forEach((f, idx) => playTone(f, f * 1.01, 0.22, "sine", 0.1, idx * 0.04));
    } else if (kind === "crystal") {
      [1244, 1568, 1975, 2489].forEach((f, idx) => playTone(f, f * 1.03, 0.24, "sine", 0.09, idx * 0.035));
    } else {
      [523, 784, 1046, 1568].forEach((f, idx) => playTone(f, f * 1.02, 0.26, "sine", 0.11, idx * 0.045));
    }
  }

  function playSfx(kind) {
    if (kind === "pickup") {
      playTone(540, 820, 0.07, "sine", 0.1, 0);
    } else if (kind === "coin") {
      playTone(987, 1318, 0.08, "sine", 0.12, 0);
      playTone(1318, 1567, 0.11, "sine", 0.11, 0.05);
    } else if (kind === "upgrade") {
      playTone(460, 440, 0.09, "triangle", 0.13, 0);
      playTone(698, 700, 0.11, "sine", 0.11, 0.07);
      playTone(932, 1046, 0.16, "sine", 0.12, 0.14);
    } else if (kind === "unlock") {
      [523, 659, 784, 1046].forEach((f, i) => {
        playTone(f, f * 1.02, 0.2, "triangle", 0.13, i * 0.07);
      });
    }
  }

  function tickBackgroundMusic(dt) {
    const mg = getMusicGain();
    if (mg <= 0.01 || !audioCtx || audioCtx.state !== "running") return;
    musicTimer += dt;
    if (musicTimer < 1.05) return;
    musicTimer = 0;
    const now = audioCtx.currentTime;
    const freq = FOREST_NOTES[musicStep % FOREST_NOTES.length];
    const harmony = FOREST_NOTES[(musicStep + 2) % FOREST_NOTES.length] * 0.5;
    musicStep++;
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.type = "sine";
    osc.frequency.setValueAtTime(freq, now);
    gain.gain.setValueAtTime(0.001, now);
    gain.gain.linearRampToValueAtTime(0.042 * mg, now + 0.04);
    gain.gain.exponentialRampToValueAtTime(0.0006, now + 0.92);
    osc.connect(gain);
    gain.connect(audioCtx.destination);
    osc.start(now);
    osc.stop(now + 0.95);
    if (musicStep % 2 === 1) {
      const pad = audioCtx.createOscillator();
      const padGain = audioCtx.createGain();
      pad.type = "triangle";
      pad.frequency.setValueAtTime(harmony, now);
      padGain.gain.setValueAtTime(0.001, now);
      padGain.gain.linearRampToValueAtTime(0.025 * mg, now + 0.18);
      padGain.gain.exponentialRampToValueAtTime(0.0006, now + 1.85);
      pad.connect(padGain);
      padGain.connect(audioCtx.destination);
      pad.start(now);
      pad.stop(now + 1.9);
    }
  }

  function haptic(kind) {
    if (window.AndroidBridge && window.AndroidBridge.triggerHaptic) {
      try { window.AndroidBridge.triggerHaptic(kind); } catch (_) {}
    }
  }

  // =========================================================================
  // 4. FLOATING UI & BANNER HELPERS
  // =========================================================================
  const floatLayer = document.getElementById("floating-layer");
  const floatingBadges = [];
  const bannerEl = document.getElementById("banner-toast");
  const joyZoneEl = document.getElementById("joystick-zone");
  let bannerTimer = null;

  function showBanner(msg) {
    bannerEl.textContent = msg;
    bannerEl.classList.add("visible");
    clearTimeout(bannerTimer);
    bannerTimer = setTimeout(function () { bannerEl.classList.remove("visible"); }, 2200);
  }

  function spawnFloatingText(worldPos, text, colorCss) {
    if (isMenuMode) return;
    const el = document.createElement("div");
    el.className = "float-badge";
    el.textContent = text;
    el.style.color = colorCss || "#ffffff";
    floatLayer.appendChild(el);
    floatingBadges.push({
      el,
      wx: worldPos.x,
      wy: worldPos.y + 1.8,
      wz: worldPos.z,
      age: 0,
      life: 1.05
    });
  }

  // =========================================================================
  // 5. PBR RENDERER, HDR STUDIO ENVIRONMENT, LIGHTING & SKY DOME
  // =========================================================================
  const canvas = document.getElementById("game-canvas");

  function createRenderer() {
    const attempts = [
      { canvas, antialias: true, alpha: false, powerPreference: "high-performance" },
      { canvas, antialias: true, alpha: false, powerPreference: "default" },
      { canvas, antialias: false, alpha: false, preserveDrawingBuffer: true, failIfMajorPerformanceCaveat: false }
    ];
    for (const opts of attempts) {
      try {
        const r = new THREE.WebGLRenderer(opts);
        if (r && r.getContext()) return r;
      } catch (err) {
        console.warn("WebGLRenderer attempt failed:", err);
      }
    }
    throw new Error("Unable to initialize WebGLRenderer");
  }

  function getViewportSize() {
    const w = Math.max(1, window.innerWidth || document.documentElement.clientWidth || canvas.clientWidth || 360);
    const h = Math.max(1, window.innerHeight || document.documentElement.clientHeight || canvas.clientHeight || 640);
    return { w, h };
  }

  const renderer = createRenderer();
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 1.75));
  renderer.setClearColor(0x6ec3eb, 1);
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.08;
  renderer.outputEncoding = THREE.sRGBEncoding;

  const scene = new THREE.Scene();
  const currentSkyColor = new THREE.Color(0x6ec3eb);
  const currentFogColor = new THREE.Color(0x9bd7f2);
  scene.background = currentSkyColor;
  scene.fog = new THREE.FogExp2(currentFogColor.getHex(), 0.0085);

  // Procedural Outdoor HDR-Style Environment Map for Realistic PBR Metal/Glass Reflections
  (function setupEnvironmentReflections() {
    try {
      const pmrem = new THREE.PMREMGenerator(renderer);
      const envScene = new THREE.Scene();
      const skyGeo = new THREE.SphereGeometry(20, 24, 16);
      const skyMat = new THREE.ShaderMaterial({
        side: THREE.BackSide,
        uniforms: {
          topColor: { value: new THREE.Color(0x38bdf8) },
          midColor: { value: new THREE.Color(0xfffbeb) },
          botColor: { value: new THREE.Color(0x1f3a2e) }
        },
        vertexShader: "varying vec3 vP; void main(){ vP=normalize(position); gl_Position=projectionMatrix*modelViewMatrix*vec4(position,1.0); }",
        fragmentShader: "uniform vec3 topColor,midColor,botColor; varying vec3 vP; void main(){ float h=vP.y; vec3 c=h>=0.0?mix(midColor,topColor,pow(h,0.55)):mix(midColor,botColor,pow(-h,0.45)); gl_FragColor=vec4(c,1.0); }"
      });
      envScene.add(new THREE.Mesh(skyGeo, skyMat));
      const sunDisc = new THREE.Mesh(new THREE.SphereGeometry(1.6, 12, 12), new THREE.MeshBasicMaterial({ color: 0xfff8dc }));
      sunDisc.position.set(8, 11, 7);
      envScene.add(sunDisc);
      const envRT = pmrem.fromScene(envScene, 0.035);
      scene.environment = envRT.texture;
      pmrem.dispose();
    } catch (e) {
      console.warn("PMREM env skipped:", e);
    }
  })();

  const initialSize = getViewportSize();
  const camera = new THREE.PerspectiveCamera(50, initialSize.w / initialSize.h, 0.4, 210);
  camera.position.set(0, 4.0, 8.2);
  camera.lookAt(0, 1.4, 2.0);

  let lastW = 0;
  let lastH = 0;
  function syncViewportSize() {
    const { w, h } = getViewportSize();
    if (w !== lastW || h !== lastH) {
      lastW = w;
      lastH = h;
      camera.aspect = w / h;
      camera.updateProjectionMatrix();
      renderer.setSize(w, h, false);
    }
  }
  syncViewportSize();
  window.addEventListener("resize", syncViewportSize);
  if (typeof ResizeObserver !== "undefined") {
    new ResizeObserver(syncViewportSize).observe(document.body);
  }

  // Atmospheric Sky Dome
  const skyUniforms = {
    uTop: { value: new THREE.Color(0x3a9bd5) },
    uHorizon: { value: new THREE.Color(0xcceeff) },
    uBottom: { value: new THREE.Color(0x285f6b) }
  };
  const skyDome = new THREE.Mesh(
    new THREE.SphereGeometry(165, 28, 18),
    new THREE.ShaderMaterial({
      side: THREE.BackSide,
      depthWrite: false,
      uniforms: skyUniforms,
      vertexShader: "varying vec3 vWorldPos; void main(){ vec4 wp = modelMatrix * vec4(position,1.0); vWorldPos = position; gl_Position = projectionMatrix * viewMatrix * wp; }",
      fragmentShader: "uniform vec3 uTop,uHorizon,uBottom; varying vec3 vWorldPos; void main(){ float y=normalize(vWorldPos).y; vec3 col = y > 0.0 ? mix(uHorizon, uTop, pow(y, 0.55)) : mix(uHorizon, uBottom, pow(-y, 0.6)); gl_FragColor=vec4(col,1.0); }"
    })
  );
  scene.add(skyDome);

  // Natural Three-Point Sun + Sky Fill + Rim Lighting
  const hemiLight = new THREE.HemisphereLight(0xfffbeb, 0x223b2c, 0.72);
  scene.add(hemiLight);

  const sunLight = new THREE.DirectionalLight(0xfff1d6, 1.35);
  sunLight.position.set(16, 26, 14);
  sunLight.castShadow = true;
  sunLight.shadow.mapSize.width = 1024;
  sunLight.shadow.mapSize.height = 1024;
  sunLight.shadow.camera.near = 2;
  sunLight.shadow.camera.far = 82;
  const d = 24;
  sunLight.shadow.camera.left = -d;
  sunLight.shadow.camera.right = d;
  sunLight.shadow.camera.top = d;
  sunLight.shadow.camera.bottom = -d;
  sunLight.shadow.bias = -0.0006;
  sunLight.shadow.normalBias = 0.02;
  scene.add(sunLight);
  scene.add(sunLight.target);

  const rimLight = new THREE.DirectionalLight(0x93c5fd, 0.42);
  rimLight.position.set(-14, 12, -16);
  scene.add(rimLight);

  function applyGraphicsQuality() {
    const q = typeof state.graphicsQuality === "number" ? state.graphicsQuality : 1;
    const dpr = window.devicePixelRatio || 1;
    if (q === 0) {
      renderer.setPixelRatio(Math.min(dpr, 1.15));
      renderer.shadowMap.enabled = false;
    } else if (q === 1) {
      renderer.setPixelRatio(Math.min(dpr, 1.65));
      renderer.shadowMap.enabled = true;
    } else {
      renderer.setPixelRatio(Math.min(dpr, 2.0));
      renderer.shadowMap.enabled = true;
    }
  }

  // =========================================================================
  // 6. HIGH-RES PROCEDURAL PBR TEXTURES & SCULPTED GEOMETRY FACTORY
  // =========================================================================
  function makeCanvasTex(size, repeatX, repeatY, isColor, drawFn) {
    const c = document.createElement("canvas");
    c.width = size;
    c.height = size;
    const ctx = c.getContext("2d");
    drawFn(ctx, size);
    const tex = new THREE.CanvasTexture(c);
    tex.wrapS = THREE.RepeatWrapping;
    tex.wrapT = THREE.RepeatWrapping;
    tex.repeat.set(repeatX || 1, repeatY || 1);
    tex.encoding = isColor ? THREE.sRGBEncoding : THREE.LinearEncoding;
    tex.anisotropy = 4;
    tex.needsUpdate = true;
    return tex;
  }

  function hash2(x, y) {
    const n = Math.sin(x * 127.1 + y * 311.7) * 43758.5453123;
    return n - Math.floor(n);
  }

  const texGrass = makeCanvasTex(256, 7, 7, true, (ctx, s) => {
    ctx.fillStyle = "#3f8f47";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 1800; i++) {
      const x = hash2(i, 1) * s;
      const y = hash2(i, 2) * s;
      const g = Math.floor(110 + hash2(i, 3) * 65);
      const r = Math.floor(38 + hash2(i, 4) * 35);
      const b = Math.floor(38 + hash2(i, 5) * 28);
      ctx.fillStyle = `rgba(${r},${g},${b},0.48)`;
      ctx.fillRect(x, y, 2 + hash2(i, 6) * 2, 5 + hash2(i, 7) * 6);
    }
    for (let i = 0; i < 180; i++) {
      ctx.fillStyle = "rgba(88, 66, 38, 0.16)";
      ctx.beginPath();
      ctx.arc(hash2(i, 9) * s, hash2(i, 10) * s, 3 + hash2(i, 11) * 5, 0, Math.PI * 2);
      ctx.fill();
    }
  });

  const texGrassBump = makeCanvasTex(256, 7, 7, false, (ctx, s) => {
    ctx.fillStyle = "#7f7f7f";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 1400; i++) {
      const v = Math.floor(85 + hash2(i, 15) * 140);
      ctx.fillStyle = `rgb(${v},${v},${v})`;
      ctx.fillRect(hash2(i, 1) * s, hash2(i, 2) * s, 2, 6);
    }
  });

  const texCobble = makeCanvasTex(256, 3, 3, true, (ctx, s) => {
    ctx.fillStyle = "#5c646d";
    ctx.fillRect(0, 0, s, s);
    const cols = 6, rows = 6;
    const cw = s / cols, ch = s / rows;
    for (let ry = 0; ry < rows; ry++) {
      for (let cx = 0; cx < cols; cx++) {
        const ox = (ry % 2) * (cw * 0.45);
        const shade = Math.floor(165 + hash2(cx, ry) * 40);
        ctx.fillStyle = `rgb(${shade},${shade - 4},${shade - 10})`;
        ctx.beginPath();
        ctx.roundRect ? ctx.roundRect((cx * cw + ox) % s + 2, ry * ch + 2, cw - 5, ch - 5, 7)
                      : ctx.fillRect((cx * cw + ox) % s + 2, ry * ch + 2, cw - 5, ch - 5);
        ctx.fill();
      }
    }
  });

  const texBark = makeCanvasTex(256, 2, 3, true, (ctx, s) => {
    ctx.fillStyle = "#5c3419";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 420; i++) {
      const x = hash2(i, 21) * s;
      const y = hash2(i, 22) * s;
      const dark = hash2(i, 23) > 0.45;
      ctx.fillStyle = dark ? "rgba(38, 18, 6, 0.55)" : "rgba(130, 82, 46, 0.38)";
      ctx.fillRect(x, y, 2 + hash2(i, 24) * 3, 18 + hash2(i, 25) * 36);
    }
  });

  const texBarkBump = makeCanvasTex(256, 2, 3, false, (ctx, s) => {
    ctx.fillStyle = "#808080";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 420; i++) {
      const v = hash2(i, 23) > 0.5 ? 45 : 210;
      ctx.fillStyle = `rgba(${v},${v},${v},0.6)`;
      ctx.fillRect(hash2(i, 21) * s, hash2(i, 22) * s, 3, 28);
    }
  });

  const texFoliage = makeCanvasTex(256, 2, 2, true, (ctx, s) => {
    ctx.fillStyle = "#1d6e38";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 900; i++) {
      const x = hash2(i, 31) * s;
      const y = hash2(i, 32) * s;
      const g = Math.floor(110 + hash2(i, 33) * 105);
      ctx.fillStyle = `rgba(34, ${g}, 58, 0.58)`;
      ctx.beginPath();
      ctx.ellipse(x, y, 4 + hash2(i, 34) * 6, 2 + hash2(i, 35) * 3, hash2(i, 36) * Math.PI, 0, Math.PI * 2);
      ctx.fill();
    }
  });

  const texRock = makeCanvasTex(256, 2, 2, true, (ctx, s) => {
    ctx.fillStyle = "#6c7886";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 650; i++) {
      const v = Math.floor(75 + hash2(i, 41) * 90);
      ctx.fillStyle = `rgba(${v},${v + 4},${v + 10},0.45)`;
      ctx.beginPath();
      ctx.arc(hash2(i, 42) * s, hash2(i, 43) * s, 3 + hash2(i, 44) * 10, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.strokeStyle = "rgba(25, 32, 42, 0.45)";
    ctx.lineWidth = 1.6;
    for (let j = 0; j < 26; j++) {
      ctx.beginPath();
      const sx = hash2(j, 45) * s, sy = hash2(j, 46) * s;
      ctx.moveTo(sx, sy);
      ctx.lineTo(sx + (hash2(j, 47) - 0.5) * 60, sy + (hash2(j, 48) - 0.5) * 40);
      ctx.stroke();
    }
  });

  const texWoodPlank = makeCanvasTex(256, 2, 2, true, (ctx, s) => {
    ctx.fillStyle = "#9c5f2b";
    ctx.fillRect(0, 0, s, s);
    const planks = 4;
    const ph = s / planks;
    for (let p = 0; p < planks; p++) {
      const shade = (p % 2 === 0) ? "#a76832" : "#925826";
      ctx.fillStyle = shade;
      ctx.fillRect(0, p * ph + 2, s, ph - 3);
      ctx.fillStyle = "rgba(48, 22, 6, 0.55)";
      ctx.fillRect(0, p * ph, s, 2);
      for (let g = 0; g < 35; g++) {
        ctx.fillStyle = "rgba(64, 32, 10, 0.16)";
        ctx.fillRect(0, p * ph + hash2(p, g) * ph, s, 1.5);
      }
      ctx.fillStyle = "#334155";
      ctx.beginPath();
      ctx.arc(14, p * ph + ph * 0.5, 2.5, 0, Math.PI * 2);
      ctx.arc(s - 14, p * ph + ph * 0.5, 2.5, 0, Math.PI * 2);
      ctx.fill();
    }
  });

  const texBrushedMetal = makeCanvasTex(256, 2, 2, true, (ctx, s) => {
    ctx.fillStyle = "#64748b";
    ctx.fillRect(0, 0, s, s);
    for (let y = 0; y < s; y += 2) {
      const v = Math.floor(90 + hash2(y, 55) * 45);
      ctx.fillStyle = `rgba(${v},${v + 8},${v + 16},0.4)`;
      ctx.fillRect(0, y, s, 1.5);
    }
    ctx.strokeStyle = "rgba(15, 23, 42, 0.5)";
    ctx.lineWidth = 3;
    ctx.strokeRect(4, 4, s - 8, s - 8);
    ctx.fillStyle = "#cbd5e1";
    [[14, 14], [s - 14, 14], [14, s - 14], [s - 14, s - 14]].forEach(([rx, ry]) => {
      ctx.beginPath();
      ctx.arc(rx, ry, 3.5, 0, Math.PI * 2);
      ctx.fill();
    });
  });

  const texHazard = makeCanvasTex(256, 3, 1, true, (ctx, s) => {
    ctx.fillStyle = "#f59e0b";
    ctx.fillRect(0, 0, s, s);
    ctx.fillStyle = "#111827";
    const step = 32;
    for (let x = -s; x < s * 2; x += step * 2) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x + step, 0);
      ctx.lineTo(x + step - s, s);
      ctx.lineTo(x - s, s);
      ctx.closePath();
      ctx.fill();
    }
  });

  const texSand = makeCanvasTex(256, 5, 5, true, (ctx, s) => {
    ctx.fillStyle = "#dfa754";
    ctx.fillRect(0, 0, s, s);
    for (let y = 0; y < s; y += 10) {
      ctx.fillStyle = "rgba(255, 232, 185, 0.14)";
      ctx.fillRect(0, y, s, 4);
      ctx.fillStyle = "rgba(160, 100, 32, 0.14)";
      ctx.fillRect(0, y + 5, s, 3);
    }
  });

  const texIce = makeCanvasTex(256, 4, 4, true, (ctx, s) => {
    ctx.fillStyle = "#d4eefc";
    ctx.fillRect(0, 0, s, s);
    ctx.strokeStyle = "rgba(125, 211, 252, 0.55)";
    ctx.lineWidth = 2;
    for (let i = 0; i < 18; i++) {
      ctx.beginPath();
      const x0 = hash2(i, 71) * s, y0 = hash2(i, 72) * s;
      ctx.moveTo(x0, y0);
      ctx.lineTo(x0 + (hash2(i, 73) - 0.5) * 90, y0 + (hash2(i, 74) - 0.5) * 90);
      ctx.stroke();
    }
  });

  const texBasalt = makeCanvasTex(256, 4, 4, true, (ctx, s) => {
    ctx.fillStyle = "#2b2423";
    ctx.fillRect(0, 0, s, s);
    ctx.strokeStyle = "rgba(239, 68, 68, 0.45)";
    ctx.lineWidth = 2;
    for (let i = 0; i < 14; i++) {
      ctx.beginPath();
      const x0 = hash2(i, 81) * s, y0 = hash2(i, 82) * s;
      ctx.moveTo(x0, y0);
      ctx.lineTo(x0 + (hash2(i, 83) - 0.5) * 70, y0 + (hash2(i, 84) - 0.5) * 70);
      ctx.stroke();
    }
  });

  const texPlaid = makeCanvasTex(256, 2, 2, true, (ctx, s) => {
    ctx.fillStyle = "#c82314";
    ctx.fillRect(0, 0, s, s);
    const step = s / 4;
    ctx.fillStyle = "rgba(25, 10, 8, 0.52)";
    for (let i = 0; i < 4; i += 2) {
      ctx.fillRect(i * step, 0, step, s);
      ctx.fillRect(0, i * step, s, step);
    }
    ctx.fillStyle = "rgba(245, 92, 42, 0.25)";
    for (let i = 1; i < 4; i += 2) {
      ctx.fillRect(i * step, 0, 4, s);
      ctx.fillRect(0, i * step, s, 4);
    }
  });

  // Helper PBR Materials
  function pbrMat(color, opts) {
    return new THREE.MeshStandardMaterial(Object.assign({
      color,
      roughness: 0.68,
      metalness: 0.08
    }, opts || {}));
  }

  function metalMat(color, opts) {
    return new THREE.MeshStandardMaterial(Object.assign({
      color,
      roughness: 0.28,
      metalness: 0.86,
      envMapIntensity: 1.25
    }, opts || {}));
  }

  // Sculpted Geometry Helpers
  function makeCapsuleGeo(r, len, radialSegs) {
    const pts = [];
    const half = len * 0.5;
    const capSteps = 5;
    for (let i = 0; i <= capSteps; i++) {
      const a = -Math.PI / 2 + (i / capSteps) * (Math.PI / 2);
      pts.push(new THREE.Vector2(Math.max(0.001, Math.cos(a) * r), -half + Math.sin(a) * r));
    }
    for (let i = 0; i <= capSteps; i++) {
      const a = (i / capSteps) * (Math.PI / 2);
      pts.push(new THREE.Vector2(Math.max(0.001, Math.cos(a) * r), half + Math.sin(a) * r));
    }
    return new THREE.LatheGeometry(pts, radialSegs || 10);
  }

  function makeOrganicGeo(radius, detail, noiseAmp, seed) {
    const geo = new THREE.IcosahedronGeometry(radius, detail || 1);
    const pos = geo.attributes.position;
    for (let i = 0; i < pos.count; i++) {
      const vx = pos.getX(i), vy = pos.getY(i), vz = pos.getZ(i);
      const n = Math.sin(vx * 3.1 + seed) * Math.cos(vy * 3.7 - seed) * Math.sin(vz * 2.9 + seed * 1.3);
      const sc = 1 + n * (noiseAmp || 0.22);
      pos.setXYZ(i, vx * sc, vy * sc, vz * sc);
    }
    geo.computeVertexNormals();
    return geo;
  }

  function makeRoundedBoxGeo(w, h, d, r) {
    const s = new THREE.Shape();
    const hw = w * 0.5 - r, hh = d * 0.5 - r;
    s.moveTo(-hw, -hh - r);
    s.lineTo(hw, -hh - r);
    s.quadraticCurveTo(hw + r, -hh - r, hw + r, -hh);
    s.lineTo(hw + r, hh);
    s.quadraticCurveTo(hw + r, hh + r, hw, hh + r);
    s.lineTo(-hw, hh + r);
    s.quadraticCurveTo(-hw - r, hh + r, -hw - r, hh);
    s.lineTo(-hw - r, -hh);
    s.quadraticCurveTo(-hw - r, -hh - r, -hw, -hh - r);
    const g = new THREE.ExtrudeGeometry(s, {
      depth: Math.max(0.02, h - r * 2),
      bevelEnabled: true,
      bevelThickness: r,
      bevelSize: r,
      bevelSegments: 2
    });
    g.rotateX(-Math.PI / 2);
    g.center();
    return g;
  }

  function makeGearGeo(outerR, innerR, teeth, thickness) {
    const shape = new THREE.Shape();
    const step = (Math.PI * 2) / (teeth * 2);
    for (let i = 0; i < teeth * 2; i++) {
      const a0 = i * step;
      const a1 = a0 + step * 0.65;
      const r = (i % 2 === 0) ? outerR : innerR;
      if (i === 0) shape.moveTo(Math.cos(a0) * r, Math.sin(a0) * r);
      else shape.lineTo(Math.cos(a0) * r, Math.sin(a0) * r);
      shape.lineTo(Math.cos(a1) * r, Math.sin(a1) * r);
    }
    shape.closePath();
    const g = new THREE.ExtrudeGeometry(shape, {
      depth: thickness,
      bevelEnabled: true,
      bevelThickness: 0.02,
      bevelSize: 0.02,
      bevelSegments: 1
    });
    g.center();
    return g;
  }

  function makeSawbladeGeo(radius, teeth, thickness) {
    const shape = new THREE.Shape();
    const step = (Math.PI * 2) / teeth;
    for (let i = 0; i < teeth; i++) {
      const a0 = i * step;
      const a1 = a0 + step * 0.55;
      const a2 = a0 + step * 0.92;
      if (i === 0) shape.moveTo(Math.cos(a0) * radius * 0.78, Math.sin(a0) * radius * 0.78);
      else shape.lineTo(Math.cos(a0) * radius * 0.78, Math.sin(a0) * radius * 0.78);
      shape.lineTo(Math.cos(a1) * radius, Math.sin(a1) * radius);
      shape.lineTo(Math.cos(a2) * radius * 0.78, Math.sin(a2) * radius * 0.78);
    }
    shape.closePath();
    const g = new THREE.ExtrudeGeometry(shape, { depth: thickness, bevelEnabled: false });
    g.center();
    return g;
  }

  // Shared PBR base geometries
  const boxGeo = new THREE.BoxGeometry(1, 1, 1);
  const coneGeo = new THREE.ConeGeometry(1, 1, 10);
  const cylGeo = new THREE.CylinderGeometry(1, 1, 1, 14);
  const sphereGeo = new THREE.SphereGeometry(1, 14, 12);
  const octaGeo = new THREE.OctahedronGeometry(1, 0);
  const organicRockGeoA = makeOrganicGeo(1.0, 2, 0.24, 1.4);
  const organicRockGeoB = makeOrganicGeo(1.0, 2, 0.26, 4.7);
  const organicCanopyGeo = makeOrganicGeo(1.0, 1, 0.22, 2.9);
  const crystalPrismGeo = new THREE.CylinderGeometry(0.14, 0.34, 1.35, 6);
  const wheelGeo = new THREE.CylinderGeometry(0.32, 0.32, 0.22, 16);
  wheelGeo.rotateZ(Math.PI / 2);
  const gearWheelGeo = makeGearGeo(0.45, 0.34, 10, 0.14);
  const sawbladeGeo = makeSawbladeGeo(0.55, 16, 0.05);

  const shadowDiscGeo = new THREE.CircleGeometry(1, 18);
  const shadowDiscMat = new THREE.MeshBasicMaterial({
    color: 0x051009,
    transparent: true,
    opacity: 0.28,
    depthWrite: false
  });

  function addContactShadow(parent, radius) {
    const sh = new THREE.Mesh(shadowDiscGeo, shadowDiscMat);
    sh.rotation.x = -Math.PI / 2;
    sh.position.y = 0.025;
    sh.scale.set(radius, radius, 1);
    parent.add(sh);
    return sh;
  }

  // Helper to build a wheeled rubber tire + metallic alloy rim
  function createWheelMesh(r, w) {
    const wg = new THREE.Group();
    const tire = new THREE.Mesh(
      new THREE.CylinderGeometry(r, r, w, 16),
      pbrMat(0x1e293b, { roughness: 0.88, metalness: 0.05 })
    );
    tire.rotation.z = Math.PI / 2;
    tire.castShadow = true;
    wg.add(tire);
    const hub = new THREE.Mesh(
      new THREE.CylinderGeometry(r * 0.56, r * 0.56, w * 1.04, 12),
      metalMat(0xcbd5e1, { roughness: 0.3, metalness: 0.88 })
    );
    hub.rotation.z = Math.PI / 2;
    wg.add(hub);
    return wg;
  }

  // Helper to build a rounded industrial storage crate / product bar
  function createProductCrate(colHex, isBar) {
    if (isBar) {
      const m = new THREE.Mesh(makeRoundedBoxGeo(0.38, 0.14, 0.2, 0.03), metalMat(colHex, { roughness: 0.22, metalness: 0.9 }));
      m.castShadow = true;
      return m;
    }
    const g = new THREE.Group();
    const body = new THREE.Mesh(makeRoundedBoxGeo(0.38, 0.32, 0.38, 0.04), pbrMat(colHex, { map: texWoodPlank, roughness: 0.58 }));
    body.castShadow = true;
    g.add(body);
    return g;
  }

  // =========================================================================
  // 7. WORLD TERRAIN, MOUNTAINS, FOLIAGE, BRIDGES & GATES
  // =========================================================================
  const worldGroup = new THREE.Group();
  scene.add(worldGroup);

  // Shimmering Lagoon Water
  const waterMesh = new THREE.Mesh(
    new THREE.PlaneGeometry(340, 340),
    pbrMat(0x287d99, { roughness: 0.16, metalness: 0.45, envMapIntensity: 1.3 })
  );
  waterMesh.rotation.x = -Math.PI / 2;
  waterMesh.position.y = -0.85;
  worldGroup.add(waterMesh);

  // Sculpted Distant Alpine Mountain Range
  for (let m = 0; m < 18; m++) {
    const ang = (m / 18) * Math.PI * 2;
    const dist = 82 + (m % 3) * 9;
    const peakH = 16 + (m % 4) * 5;
    const peakR = 11 + (m % 3) * 3;
    const mtn = new THREE.Group();
    mtn.position.set(Math.cos(ang) * dist, -0.9, Math.sin(ang) * dist);
    const mGeo = new THREE.ConeGeometry(peakR, peakH, 9, 3);
    const mPos = mGeo.attributes.position;
    for (let vi = 0; vi < mPos.count; vi++) {
      const py = mPos.getY(vi);
      if (py < peakH * 0.42 && py > -peakH * 0.45) {
        mPos.setX(vi, mPos.getX(vi) * (1 + hash2(vi, m) * 0.28 - 0.14));
        mPos.setZ(vi, mPos.getZ(vi) * (1 + hash2(m, vi) * 0.28 - 0.14));
      }
    }
    mGeo.computeVertexNormals();
    const body = new THREE.Mesh(mGeo, pbrMat(m % 2 === 0 ? 0x3b6e52 : 0x475569, { map: texRock, roughness: 0.85 }));
    body.position.y = peakH * 0.5;
    mtn.add(body);
    const cap = new THREE.Mesh(new THREE.ConeGeometry(peakR * 0.36, peakH * 0.35, 9), pbrMat(0xf1f5f9, { roughness: 0.4 }));
    cap.position.y = peakH * 0.82;
    mtn.add(cap);
    worldGroup.add(mtn);
  }

  // Biome Platforms with Textured PBR Ground & Natural Cliff Skirts
  const biomeFloorTex = {
    forest: texGrass,
    mountains: texRock,
    desert: texSand,
    frozen: texIce,
    volcanic: texBasalt,
    jungle: texGrass
  };

  BIOMES.forEach((b, bi) => {
    const padGeo = makeRoundedBoxGeo(31.8, 1.0, 31.8, 0.28);
    const padMat = pbrMat(b.id === "forest" ? 0xffffff : b.ground, {
      map: biomeFloorTex[b.id] || texRock,
      bumpMap: b.id === "forest" ? texGrassBump : undefined,
      bumpScale: 0.08,
      roughness: b.id === "frozen" ? 0.35 : 0.82,
      metalness: b.id === "frozen" ? 0.12 : 0.02
    });
    const pad = new THREE.Mesh(padGeo, padMat);
    pad.position.set(b.cx, -0.5, b.cz);
    pad.receiveShadow = true;
    worldGroup.add(pad);

    const cliff = new THREE.Mesh(
      makeRoundedBoxGeo(32.8, 0.8, 32.8, 0.22),
      pbrMat(b.id === "forest" ? 0x6b4f2d : 0x475569, { map: texRock, roughness: 0.9 })
    );
    cliff.position.set(b.cx, -0.95, b.cz);
    worldGroup.add(cliff);

    // Sculpted shoreline cliff boulders around platform corners
    [[-15.4, -15.4], [15.4, -15.4], [-15.4, 15.4], [15.4, 15.4]].forEach(([rx, rz], ci) => {
      const cr = new THREE.Mesh(organicRockGeoA, pbrMat(0x52606d, { map: texRock, roughness: 0.9 }));
      cr.scale.set(1.6, 0.85, 1.6);
      cr.position.set(b.cx + rx, -0.65, b.cz + rz);
      cr.rotation.y = ci + bi;
      worldGroup.add(cr);
    });
  });

  // Central Cobble Courtyard & Natural Stepping Flagstones
  const plaza = new THREE.Mesh(
    new THREE.CylinderGeometry(6.8, 7.2, 0.12, 24),
    pbrMat(0xffffff, { map: texCobble, roughness: 0.78 })
  );
  plaza.position.set(0, 0.02, 0.5);
  plaza.receiveShadow = true;
  worldGroup.add(plaza);

  const stoneCurb = new THREE.Mesh(
    new THREE.TorusGeometry(6.9, 0.18, 8, 32),
    pbrMat(0x94a3b8, { map: texRock, roughness: 0.85 })
  );
  stoneCurb.rotation.x = Math.PI / 2;
  stoneCurb.position.set(0, 0.05, 0.5);
  worldGroup.add(stoneCurb);

  [
    [0, -8.4], [0, -11.4], [0, -14.0],
    [8.2, 0], [11.2, 0], [-8.4, 0], [-11.4, 0],
    [-2.4, 3.6], [-3.6, 4.6]
  ].forEach(([sx, sz], si) => {
    const st = new THREE.Mesh(
      makeOrganicGeo(0.75, 1, 0.16, si + 3.1),
      pbrMat(0xcbd5e1, { map: texCobble, roughness: 0.8 })
    );
    st.scale.set(1.0, 0.12, 0.95);
    st.position.set(sx, 0.04, sz);
    st.receiveShadow = true;
    worldGroup.add(st);
  });

  // Arched Timber Bridges with Post Lanterns
  function addBridge(x, z, rotY) {
    const brGroup = new THREE.Group();
    brGroup.position.set(x, -0.22, z);
    brGroup.rotation.y = rotY;
    for (let pi = -3; pi <= 3; pi++) {
      const plank = new THREE.Mesh(
        makeRoundedBoxGeo(5.4, 0.24, 0.72, 0.05),
        pbrMat(0xffffff, { map: texWoodPlank, roughness: 0.72 })
      );
      plank.position.set(0, 0.18 - Math.abs(pi) * 0.02, pi * 0.82);
      plank.receiveShadow = true;
      plank.castShadow = true;
      brGroup.add(plank);
    }
    [-2.55, 2.55].forEach((rx) => {
      const beam = new THREE.Mesh(makeRoundedBoxGeo(0.26, 0.22, 5.8, 0.04), pbrMat(0x78350f, { map: texBark }));
      beam.position.set(rx, 0.68, 0);
      brGroup.add(beam);
      [-2.4, 0, 2.4].forEach((pz) => {
        const post = new THREE.Mesh(makeCapsuleGeo(0.14, 0.75, 8), pbrMat(0x5c3419, { map: texBark }));
        post.position.set(rx, 0.45, pz);
        post.castShadow = true;
        brGroup.add(post);
      });
    });
    worldGroup.add(brGroup);
  }
  addBridge(0, -17, 0);
  addBridge(17, 0, Math.PI / 2);
  addBridge(-17, 0, Math.PI / 2);
  addBridge(0, 17, 0);
  addBridge(34, -17, 0);

  // Detailed Wildflowers, Grass Blades & Organic Bushes
  const grassTufts = [];
  const grassCoords = [
    [-7.5, 4.5], [-8.8, -4.2], [7.8, -1.8], [3.5, 8.2], [-1.5, 9.6],
    [-9.5, 7.2], [9.2, 4.2], [-5.8, -8.5], [6.2, -8.8], [12.5, -5.5],
    [-12.2, -6.2], [8.5, 11.5], [-13.0, 11.2], [13.2, -12.0], [-4.2, 9.5], [4.4, -9.8]
  ];
  const flowerColors = [0xfde047, 0xf472b6, 0xfef9c3, 0x60a5fa];
  const bladeShape = new THREE.Shape();
  bladeShape.moveTo(-0.07, 0);
  bladeShape.quadraticCurveTo(0.02, 0.28, 0.04, 0.52);
  bladeShape.quadraticCurveTo(0.08, 0.26, 0.07, 0);
  const curvedBladeGeo = new THREE.ExtrudeGeometry(bladeShape, { depth: 0.02, bevelEnabled: false });

  grassCoords.forEach(([gx, gz], idx) => {
    const tuft = new THREE.Group();
    tuft.position.set(gx, 0, gz);
    for (let b = 0; b < 4; b++) {
      const blade = new THREE.Mesh(curvedBladeGeo, pbrMat(b % 2 === 0 ? 0x22c55e : 0x16a34a, { roughness: 0.6 }));
      blade.rotation.y = b * 1.4;
      blade.rotation.z = (b - 1.5) * 0.16;
      tuft.add(blade);
    }
    if (idx % 2 === 0) {
      const fCol = flowerColors[idx % flowerColors.length];
      const fCenter = new THREE.Mesh(sphereGeo, pbrMat(0xf59e0b, { roughness: 0.4 }));
      fCenter.scale.setScalar(0.08);
      fCenter.position.set(0.06, 0.48, 0.05);
      tuft.add(fCenter);
      for (let pt = 0; pt < 5; pt++) {
        const pAng = (pt / 5) * Math.PI * 2;
        const petal = new THREE.Mesh(sphereGeo, pbrMat(fCol, { emissive: fCol, emissiveIntensity: 0.15, roughness: 0.4 }));
        petal.scale.set(0.07, 0.03, 0.07);
        petal.position.set(0.06 + Math.cos(pAng) * 0.09, 0.48, 0.05 + Math.sin(pAng) * 0.09);
        tuft.add(petal);
      }
    }
    worldGroup.add(tuft);
    grassTufts.push(tuft);
  });

  [[-12.5, 8.5], [12.8, 7.5], [-13.4, -10.5], [12.2, -12.2], [7.2, 12.6], [-8.4, 13.0]].forEach(([bx, bz], i) => {
    const bush = new THREE.Group();
    bush.position.set(bx, 0, bz);
    addContactShadow(bush, 1.05);
    for (let c = 0; c < 3; c++) {
      const lobe = new THREE.Mesh(organicCanopyGeo, pbrMat(c === 0 ? 0x15803d : 0x22c55e, { map: texFoliage, roughness: 0.78 }));
      const sc = 0.68 - c * 0.12;
      lobe.scale.set(sc * 1.15, sc * 0.85, sc * 1.15);
      lobe.position.set((c - 1) * 0.34, 0.36 + (c === 1 ? 0.16 : 0), (c % 2) * 0.2);
      lobe.castShadow = true;
      bush.add(lobe);
    }
    worldGroup.add(bush);
  });

  // Soft Multi-Lobe Volumetric Clouds
  const skyClouds = [];
  [[-14, 16, -18], [12, 18, -24], [-6, 17, 14], [18, 16, 8], [-24, 17, -6]].forEach(([cx, cy, cz], idx) => {
    const cg = new THREE.Group();
    cg.position.set(cx, cy, cz);
    const puffMat = pbrMat(0xffffff, { roughness: 0.95, metalness: 0.0, emissive: 0xffffff, emissiveIntensity: 0.18 });
    [[0, 0, 0, 1.6, 0.75, 1.2], [-1.15, -0.15, 0.2, 1.1, 0.55, 0.95], [1.2, -0.1, -0.15, 1.2, 0.6, 0.95], [0.2, 0.35, 0.1, 0.95, 0.55, 0.85]].forEach(([px, py, pz, sx, sy, sz], pi) => {
      const puff = new THREE.Mesh(makeOrganicGeo(1, 1, 0.16, idx + pi), puffMat);
      puff.scale.set(sx, sy, sz);
      puff.position.set(px, py, pz);
      cg.add(puff);
    });
    scene.add(cg);
    skyClouds.push(cg);
  });

  // Glowing Atmospheric Fireflies
  const fireflies = [];
  for (let fi = 0; fi < 22; fi++) {
    const fMesh = new THREE.Mesh(
      sphereGeo,
      new THREE.MeshBasicMaterial({ color: fi % 2 === 0 ? 0xfef08a : 0xa7f3d0 })
    );
    fMesh.scale.setScalar(0.065);
    const fx = (Math.random() - 0.5) * 24;
    const fy = 0.9 + Math.random() * 2.4;
    const fz = (Math.random() - 0.5) * 24;
    fMesh.position.set(fx, fy, fz);
    scene.add(fMesh);
    fireflies.push({ mesh: fMesh, baseX: fx, baseY: fy, baseZ: fz, phase: Math.random() * Math.PI * 2, speed: 0.7 + Math.random() * 0.8 });
  }

  // Detailed Frontier Trading Post Pavilion (Sell Zone) at (-4.6, 5.4)
  const SELL_ZONE = { x: -4.6, z: 5.4, radius: 3.1 };
  const sellPadGroup = new THREE.Group();
  sellPadGroup.position.set(SELL_ZONE.x, 0.05, SELL_ZONE.z);
  worldGroup.add(sellPadGroup);

  const sellRing = new THREE.Mesh(
    new THREE.RingGeometry(2.3, 2.85, 32),
    new THREE.MeshBasicMaterial({ color: 0xf6b73c, side: THREE.DoubleSide })
  );
  sellRing.rotation.x = -Math.PI / 2;
  sellRing.position.y = 0.04;
  sellPadGroup.add(sellRing);

  const sellFloor = new THREE.Mesh(
    new THREE.CylinderGeometry(2.3, 2.45, 0.12, 24),
    pbrMat(0xf59e0b, { map: texCobble, emissive: 0x78350f, emissiveIntensity: 0.25, roughness: 0.65 })
  );
  sellFloor.receiveShadow = true;
  sellPadGroup.add(sellFloor);

  const stall = new THREE.Group();
  stall.position.set(-1.95, 0, 1.95);
  addContactShadow(stall, 2.2);
  // Timber deck & posts
  const deck = new THREE.Mesh(makeRoundedBoxGeo(2.5, 0.2, 2.0, 0.04), pbrMat(0xffffff, { map: texWoodPlank }));
  deck.position.y = 0.1;
  deck.receiveShadow = true;
  stall.add(deck);
  for (const [px, pz] of [[-0.95, -0.75], [0.95, -0.75], [-0.95, 0.75], [0.95, 0.75]]) {
    const post = new THREE.Mesh(makeCapsuleGeo(0.09, 1.85, 8), pbrMat(0x78350f, { map: texBark }));
    post.position.set(px, 1.0, pz);
    post.castShadow = true;
    stall.add(post);
  }
  // Pitched Gable Roof (ExtrudeGeometry)
  const roofShape = new THREE.Shape();
  roofShape.moveTo(-1.45, 0);
  roofShape.lineTo(0, 0.85);
  roofShape.lineTo(1.45, 0);
  roofShape.lineTo(1.25, -0.1);
  roofShape.lineTo(0, 0.68);
  roofShape.lineTo(-1.25, -0.1);
  roofShape.closePath();
  const pitchedRoofGeo = new THREE.ExtrudeGeometry(roofShape, { depth: 2.25, bevelEnabled: true, bevelThickness: 0.03, bevelSize: 0.03 });
  pitchedRoofGeo.center();
  const roof = new THREE.Mesh(pitchedRoofGeo, pbrMat(0xb91c1c, { map: texWoodPlank, roughness: 0.6 }));
  roof.position.set(0, 2.28, 0);
  roof.castShadow = true;
  stall.add(roof);

  // Market Counter, Wooden Barrel & Hanging Lantern
  const counter = new THREE.Mesh(makeRoundedBoxGeo(1.7, 0.75, 0.6, 0.04), pbrMat(0xd97706, { map: texWoodPlank }));
  counter.position.set(0, 0.52, -0.55);
  counter.castShadow = true;
  stall.add(counter);

  const barrelPts = [new THREE.Vector2(0.01, 0), new THREE.Vector2(0.24, 0.02), new THREE.Vector2(0.3, 0.3), new THREE.Vector2(0.24, 0.58), new THREE.Vector2(0.01, 0.6)];
  const barrel = new THREE.Mesh(new THREE.LatheGeometry(barrelPts, 14), pbrMat(0x92400e, { map: texWoodPlank }));
  barrel.position.set(1.45, 0.02, -0.2);
  barrel.castShadow = true;
  stall.add(barrel);

  const lantern = new THREE.Mesh(sphereGeo, pbrMat(0xfef08a, { emissive: 0xf59e0b, emissiveIntensity: 1.1, roughness: 0.2 }));
  lantern.scale.set(0.14, 0.2, 0.14);
  lantern.position.set(0.95, 1.68, -0.78);
  stall.add(lantern);

  const coinSign = new THREE.Mesh(
    new THREE.CylinderGeometry(0.54, 0.54, 0.12, 20),
    metalMat(0xfbbf24, { emissive: 0xd97706, emissiveIntensity: 0.35, roughness: 0.22, metalness: 0.92 })
  );
  coinSign.rotation.x = Math.PI / 2;
  coinSign.position.set(0, 3.15, 0);
  stall.add(coinSign);
  sellPadGroup.add(stall);

  // Stone Arch Biome Gates
  const biomeGates = {};
  BIOMES.forEach((b) => {
    if (!b.gate) return;
    const gGroup = new THREE.Group();
    gGroup.position.set(b.gate.x, 0, b.gate.z);
    if (b.gate.dir === "x") gGroup.rotation.y = Math.PI / 2;

    [-2.4, 2.4].forEach((px) => {
      const pillar = new THREE.Mesh(makeRoundedBoxGeo(0.85, 3.2, 0.85, 0.08), pbrMat(0x64748b, { map: texCobble, roughness: 0.8 }));
      pillar.position.set(px, 1.6, 0);
      pillar.castShadow = true;
      gGroup.add(pillar);
      const cap = new THREE.Mesh(makeOrganicGeo(0.52, 1, 0.15, px), pbrMat(b.accent, { emissive: b.accent, emissiveIntensity: 0.4, roughness: 0.3 }));
      cap.position.set(px, 3.35, 0);
      gGroup.add(cap);
    });

    const barrier = new THREE.Mesh(
      makeRoundedBoxGeo(4.1, 2.5, 0.24, 0.06),
      new THREE.MeshStandardMaterial({
        color: b.accent,
        transparent: true,
        opacity: 0.48,
        emissive: b.accent,
        emissiveIntensity: 0.55,
        roughness: 0.15,
        metalness: 0.4
      })
    );
    barrier.position.set(0, 1.35, 0);
    gGroup.add(barrier);

    const padRing = new THREE.Mesh(
      new THREE.RingGeometry(1.4, 1.85, 24),
      new THREE.MeshBasicMaterial({ color: 0xfbbf24, side: THREE.DoubleSide })
    );
    padRing.rotation.x = -Math.PI / 2;
    padRing.position.set(0, 0.08, 0);
    gGroup.add(padRing);

    worldGroup.add(gGroup);
    biomeGates[b.id] = { biome: b, group: gGroup, barrier, padRing, charge: 0 };
  });

  // Detailed World Buildings + Quad-Rotor Worker Drones
  const worldBuildings = {};
  BUILDING_DEFS.forEach((def) => {
    const grp = new THREE.Group();
    grp.position.set(def.x, 0, def.z);
    addContactShadow(grp, 2.1);

    const padMesh = new THREE.Mesh(new THREE.CylinderGeometry(1.95, 2.08, 0.14, 24), pbrMat(0x334155, { map: texBrushedMetal, roughness: 0.55 }));
    padMesh.position.y = 0.06;
    padMesh.receiveShadow = true;
    grp.add(padMesh);

    const ring = new THREE.Mesh(
      new THREE.RingGeometry(1.68, 1.98, 28),
      new THREE.MeshBasicMaterial({ color: 0x10b981, side: THREE.DoubleSide })
    );
    ring.rotation.x = -Math.PI / 2;
    ring.position.y = 0.14;
    grp.add(ring);

    const struct = new THREE.Group();
    struct.visible = false;

    const baseHull = new THREE.Mesh(
      makeRoundedBoxGeo(2.2, 1.35, 2.2, 0.08),
      pbrMat(def.id === "smelter" ? 0x475569 : (def.id === "warehouse" ? 0x1e3a8a : 0x92400e), {
        map: def.id === "sawmill" ? texWoodPlank : texBrushedMetal,
        roughness: 0.52,
        metalness: def.id === "sawmill" ? 0.15 : 0.45
      })
    );
    baseHull.position.y = 0.78;
    baseHull.castShadow = true;
    struct.add(baseHull);

    // Pitched industrial roof + exhaust chimney
    const bRoof = new THREE.Mesh(
      new THREE.ConeGeometry(1.75, 0.75, 4),
      pbrMat(def.id === "smelter" ? 0x991b1b : 0xb45309, { roughness: 0.55 })
    );
    bRoof.rotation.y = Math.PI / 4;
    bRoof.position.y = 1.82;
    bRoof.castShadow = true;
    struct.add(bRoof);

    const chimney = new THREE.Mesh(
      new THREE.CylinderGeometry(0.16, 0.22, 1.1, 10),
      metalMat(0x334155)
    );
    chimney.position.set(0.65, 1.9, -0.55);
    struct.add(chimney);

    const spinner = new THREE.Mesh(
      gearWheelGeo,
      metalMat(def.id === "smelter" ? 0xf97316 : 0x38bdf8, {
        emissive: def.id === "smelter" ? 0xea580c : 0x0284c7,
        emissiveIntensity: 0.35
      })
    );
    spinner.scale.setScalar(1.35);
    spinner.rotation.x = Math.PI / 2;
    spinner.position.y = 2.38;
    spinner.castShadow = true;
    struct.add(spinner);

    // Modular Production Line Bays (max 3)
    const lineModules = [];
    for (let li = 0; li < 3; li++) {
      const lm = new THREE.Mesh(
        makeRoundedBoxGeo(0.75, 0.75, 0.75, 0.06),
        metalMat(0xfbbf24, { roughness: 0.35, emissive: 0xd97706, emissiveIntensity: 0.15 })
      );
      const lang = li * (Math.PI * 2 / 3);
      lm.position.set(Math.cos(lang) * 1.42, 0.48, Math.sin(lang) * 1.42);
      lm.visible = false;
      struct.add(lm);
      lineModules.push(lm);
    }

    // Orbiting Quad-Rotor Industrial Worker Drones (max 4)
    const workerGroup = new THREE.Group();
    struct.add(workerGroup);
    const workerMeshes = [];
    for (let wi = 0; wi < 4; wi++) {
      const drone = new THREE.Group();
      const chassis = new THREE.Mesh(makeRoundedBoxGeo(0.32, 0.14, 0.32, 0.04), metalMat(0x0ea5e9));
      drone.add(chassis);
      const eye = new THREE.Mesh(sphereGeo, pbrMat(0x22d3ee, { emissive: 0x22d3ee, emissiveIntensity: 1.0 }));
      eye.scale.setScalar(0.08);
      eye.position.set(0, 0, 0.17);
      drone.add(eye);
      for (const [rx, rz] of [[-0.2, -0.2], [0.2, -0.2], [-0.2, 0.2], [0.2, 0.2]]) {
        const rotor = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 0.02, 8), metalMat(0xe2e8f0));
        rotor.position.set(rx, 0.1, rz);
        drone.add(rotor);
      }
      const wang = wi * (Math.PI / 2);
      drone.position.set(Math.cos(wang) * 2.2, 1.55, Math.sin(wang) * 2.2);
      drone.visible = false;
      workerGroup.add(drone);
      workerMeshes.push(drone);
    }

    grp.add(struct);
    worldGroup.add(grp);
    worldBuildings[def.id] = {
      def, group: grp, padMesh, ring, struct, spinner,
      lineModules, workerGroup, workerMeshes, charge: 0
    };
  });

  // =========================================================================
  // 8. DETAILED 3D INDUSTRIAL MACHINERY (10 MACHINES) & FLEET VEHICLES (5)
  // =========================================================================
  const worldMachines = {};
  const machineSmoke = [];

  function spawnMachineSmoke(wx, wy, wz, col) {
    if (machineSmoke.length > 28) return;
    const m = new THREE.Mesh(
      makeOrganicGeo(0.22, 0, 0.15, wx),
      new THREE.MeshStandardMaterial({ color: col || 0xd1d5db, transparent: true, opacity: 0.55, roughness: 0.95 })
    );
    m.position.set(wx + (Math.random() - 0.5) * 0.15, wy, wz + (Math.random() - 0.5) * 0.15);
    scene.add(m);
    machineSmoke.push({ mesh: m, age: 0, life: 1.35, vy: 1.1 + Math.random() * 0.4 });
  }

  function createMachineModel(def) {
    const grp = new THREE.Group();
    grp.position.set(def.x, 0, def.z);
    grp.rotation.y = def.rotY;
    addContactShadow(grp, def.id === "conveyor_belt" ? 2.6 : 1.7);

    // Subtle Blueprint Foundation Pad (visible when biome unlocked, machine not yet bought)
    const pad = new THREE.Mesh(
      makeRoundedBoxGeo(def.id === "conveyor_belt" ? 2.0 : 2.3, 0.12, def.id === "conveyor_belt" ? 5.6 : 2.3, 0.04),
      pbrMat(0x1e293b, { map: texBrushedMetal, roughness: 0.65 })
    );
    pad.position.y = 0.06;
    pad.receiveShadow = true;
    grp.add(pad);

    const hazardTrim = new THREE.Mesh(
      new THREE.BoxGeometry(def.id === "conveyor_belt" ? 2.08 : 2.38, 0.08, def.id === "conveyor_belt" ? 5.68 : 2.38),
      pbrMat(0xffffff, { map: texHazard, roughness: 0.5 })
    );
    hazardTrim.position.y = 0.045;
    grp.add(hazardTrim);

    const rig = new THREE.Group();
    rig.visible = false;
    grp.add(rig);

    // Control Console + Glowing Status LED (Green = Running, Red = Paused)
    const consoleBox = new THREE.Mesh(makeRoundedBoxGeo(0.42, 0.85, 0.36, 0.04), metalMat(0x334155));
    consoleBox.position.set(-0.92, 0.52, 0.88);
    rig.add(consoleBox);
    const statusLed = new THREE.Mesh(sphereGeo, pbrMat(0x22c55e, { emissive: 0x22c55e, emissiveIntensity: 1.3 }));
    statusLed.scale.setScalar(0.09);
    statusLed.position.set(-0.92, 1.0, 0.92);
    rig.add(statusLed);

    const anim = { rotParts: [], pistons: [], crates: [], smokeOrigin: null };

    if (def.id === "wood_cutter") {
      const frame = new THREE.Mesh(makeRoundedBoxGeo(1.9, 0.72, 1.5, 0.05), metalMat(0xb45309));
      frame.position.y = 0.48;
      frame.castShadow = true;
      rig.add(frame);
      const table = new THREE.Mesh(makeRoundedBoxGeo(2.05, 0.12, 1.3, 0.02), metalMat(0xcbd5e1));
      table.position.y = 0.88;
      rig.add(table);
      const blade = new THREE.Mesh(sawbladeGeo, metalMat(0xe2e8f0, { metalness: 0.95, roughness: 0.18 }));
      blade.rotation.y = Math.PI / 2;
      blade.position.set(0, 1.06, 0);
      blade.castShadow = true;
      rig.add(blade);
      anim.rotParts.push({ mesh: blade, axis: "x", speed: 9.5 });
      const guard = new THREE.Mesh(new THREE.TorusGeometry(0.44, 0.06, 8, 14, Math.PI), metalMat(0xf59e0b));
      guard.rotation.y = Math.PI / 2;
      guard.position.set(0, 1.08, 0);
      rig.add(guard);
      const plankOut = createProductCrate(0xd97706, true);
      plankOut.position.set(0.65, 1.02, 0.15);
      rig.add(plankOut);
      anim.crates.push({ mesh: plankOut, axis: "x", min: -0.6, max: 0.8, speed: 0.65 });
    } else if (def.id === "stone_crusher") {
      const chassis = new THREE.Mesh(makeRoundedBoxGeo(1.85, 1.0, 1.7, 0.06), metalMat(0x475569));
      chassis.position.y = 0.62;
      chassis.castShadow = true;
      rig.add(chassis);
      const hopper = new THREE.Mesh(new THREE.CylinderGeometry(0.92, 0.45, 0.75, 4), metalMat(0x64748b));
      hopper.rotation.y = Math.PI / 4;
      hopper.position.y = 1.48;
      rig.add(hopper);
      [-0.26, 0.26].forEach((ox, oi) => {
        const roller = new THREE.Mesh(gearWheelGeo, metalMat(0xcbd5e1));
        roller.scale.set(1.1, 1.1, 2.2);
        roller.position.set(ox, 1.24, 0);
        rig.add(roller);
        anim.rotParts.push({ mesh: roller, axis: "z", speed: oi === 0 ? 5.2 : -5.2 });
      });
      const pistonArm = new THREE.Mesh(new THREE.CylinderGeometry(0.11, 0.11, 0.75, 10), metalMat(0xfbbf24));
      pistonArm.position.set(0.75, 1.15, 0.5);
      rig.add(pistonArm);
      anim.pistons.push({ mesh: pistonArm, baseY: 1.15, amp: 0.24, speed: 6.0 });
      const block = createProductCrate(0x94a3b8, false);
      block.scale.setScalar(0.85);
      block.position.set(-0.6, 0.45, 0.72);
      rig.add(block);
      anim.crates.push({ mesh: block, axis: "z", min: 0.25, max: 0.95, speed: 0.45 });
    } else if (def.id === "conveyor_belt") {
      for (const rz of [-2.3, 0, 2.3]) {
        for (const rx of [-0.62, 0.62]) {
          const leg = new THREE.Mesh(makeCapsuleGeo(0.08, 0.55, 8), metalMat(0x334155));
          leg.position.set(rx, 0.32, rz);
          rig.add(leg);
        }
      }
      const railL = new THREE.Mesh(makeRoundedBoxGeo(0.14, 0.22, 5.2, 0.03), metalMat(0x0284c7));
      railL.position.set(-0.68, 0.72, 0);
      rig.add(railL);
      const railR = railL.clone();
      railR.position.x = 0.68;
      rig.add(railR);
      const beltBed = new THREE.Mesh(makeRoundedBoxGeo(1.18, 0.14, 5.05, 0.03), pbrMat(0x1e293b, { roughness: 0.88 }));
      beltBed.position.y = 0.68;
      beltBed.receiveShadow = true;
      rig.add(beltBed);
      for (let ri = -2.4; ri <= 2.4; ri += 1.2) {
        const roller = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 1.24, 12), metalMat(0x94a3b8));
        roller.rotation.z = Math.PI / 2;
        roller.position.set(0, 0.66, ri);
        rig.add(roller);
        anim.rotParts.push({ mesh: roller, axis: "x", speed: 6.0 });
      }
      [0xd97706, 0xcbd5e1, 0xfbbf24, 0x22d3ee].forEach((cCol, ci) => {
        const cr = createProductCrate(cCol, ci % 2 === 1);
        cr.position.set(0, 0.92, -2.0 + ci * 1.2);
        rig.add(cr);
        anim.crates.push({ mesh: cr, axis: "z", min: -2.2, max: 2.2, speed: 1.1 });
      });
    } else if (def.id === "sawmill_machine") {
      const bay = new THREE.Mesh(makeRoundedBoxGeo(2.0, 1.35, 1.6, 0.06), metalMat(0x92400e, { map: texWoodPlank, metalness: 0.25 }));
      bay.position.y = 0.78;
      bay.castShadow = true;
      rig.add(bay);
      [-0.55, 0.55].forEach((fx) => {
        const fw = new THREE.Mesh(gearWheelGeo, metalMat(0xf59e0b));
        fw.scale.setScalar(1.1);
        fw.position.set(fx, 1.55, 0);
        rig.add(fw);
        anim.rotParts.push({ mesh: fw, axis: "z", speed: 5.5 });
      });
      const crateOut = createProductCrate(0xf59e0b, false);
      crateOut.position.set(0.55, 0.9, 0.85);
      rig.add(crateOut);
      anim.crates.push({ mesh: crateOut, axis: "z", min: 0.2, max: 0.95, speed: 0.55 });
    } else if (def.id === "auto_miner_machine") {
      // Tripod Gantry + Helical Rotary Drill Auger
      [[-0.75, -0.7], [0.75, -0.7], [0, 0.8]].forEach(([tx, tz]) => {
        const strut = new THREE.Mesh(makeCapsuleGeo(0.08, 1.9, 8), metalMat(0x059669));
        strut.position.set(tx * 0.65, 1.05, tz * 0.65);
        strut.lookAt(0, 2.1, 0);
        rig.add(strut);
      });
      const augerGroup = new THREE.Group();
      augerGroup.position.set(0, 1.15, 0);
      const shaft = new THREE.Mesh(new THREE.CylinderGeometry(0.14, 0.04, 1.55, 12), metalMat(0xe2e8f0));
      augerGroup.add(shaft);
      for (let hi = 0; hi < 5; hi++) {
        const fin = new THREE.Mesh(new THREE.CylinderGeometry(0.36 - hi * 0.05, 0.36 - hi * 0.05, 0.06, 10), metalMat(0xfbbf24));
        fin.position.y = 0.55 - hi * 0.28;
        fin.rotation.x = 0.26;
        fin.rotation.y = hi * 0.9;
        augerGroup.add(fin);
      }
      rig.add(augerGroup);
      anim.rotParts.push({ mesh: augerGroup, axis: "y", speed: 7.5 });
      anim.pistons.push({ mesh: augerGroup, baseY: 1.12, amp: 0.16, speed: 3.8 });
    } else if (def.id === "ore_processor") {
      // Inclined Rotary Trommel Drum + Sifting Hopper
      const stand = new THREE.Mesh(makeRoundedBoxGeo(1.9, 0.5, 1.5, 0.04), metalMat(0x334155));
      stand.position.y = 0.35;
      rig.add(stand);
      const drum = new THREE.Mesh(new THREE.CylinderGeometry(0.55, 0.55, 1.55, 14), metalMat(0xfb923c, { map: texBrushedMetal }));
      drum.rotation.z = Math.PI / 2 - 0.18;
      drum.position.set(0, 1.1, 0);
      drum.castShadow = true;
      rig.add(drum);
      anim.rotParts.push({ mesh: drum, axis: "x", speed: 4.5 });
      const conc = createProductCrate(0xea580c, true);
      conc.position.set(0.6, 0.65, 0.55);
      rig.add(conc);
      anim.crates.push({ mesh: conc, axis: "x", min: -0.4, max: 0.75, speed: 0.5 });
    } else if (def.id === "smelting_furnace") {
      // Blast Furnace + Glowing Molten Crucible + Smokestack + Ingot Bars
      const base = new THREE.Mesh(new THREE.CylinderGeometry(0.88, 1.05, 1.55, 12), pbrMat(0x7f1d1d, { map: texCobble, roughness: 0.85 }));
      base.position.y = 0.85;
      base.castShadow = true;
      rig.add(base);
      const stack = new THREE.Mesh(new THREE.CylinderGeometry(0.28, 0.42, 1.4, 12), metalMat(0x1e293b));
      stack.position.set(-0.25, 2.05, -0.2);
      rig.add(stack);
      const hearth = new THREE.Mesh(sphereGeo, pbrMat(0xff6b00, { emissive: 0xff4500, emissiveIntensity: 1.4, roughness: 0.2 }));
      hearth.scale.set(0.45, 0.38, 0.28);
      hearth.position.set(0, 0.58, 0.88);
      rig.add(hearth);
      const bar = createProductCrate(0x94a3b8, true);
      bar.position.set(0.68, 0.35, 0.85);
      rig.add(bar);
      anim.crates.push({ mesh: bar, axis: "z", min: 0.45, max: 1.0, speed: 0.4 });
      anim.smokeOrigin = { lx: -0.25, ly: 2.85, lz: -0.2, col: 0x9ca3af };
    } else if (def.id === "gold_refinery") {
      // Pressurized Twin Copper Reaction Tanks + TubeGeometry Conduit + Gold Centrifuge
      [-0.55, 0.55].forEach((tx) => {
        const tank = new THREE.Mesh(makeCapsuleGeo(0.36, 1.15, 14), metalMat(0xd97706, { roughness: 0.24, metalness: 0.9 }));
        tank.position.set(tx, 0.95, -0.2);
        tank.castShadow = true;
        rig.add(tank);
      });
      const pipeCurve = new THREE.CatmullRomCurve3([new THREE.Vector3(-0.55, 1.6, -0.2), new THREE.Vector3(0, 1.95, 0.25), new THREE.Vector3(0.55, 1.6, -0.2)]);
      const pipe = new THREE.Mesh(new THREE.TubeGeometry(pipeCurve, 12, 0.08, 8, false), metalMat(0xfde047));
      rig.add(pipe);
      const rotor = new THREE.Mesh(gearWheelGeo, metalMat(0xfbbf24, { emissive: 0xd97706, emissiveIntensity: 0.3 }));
      rotor.rotation.x = Math.PI / 2;
      rotor.position.set(0, 0.92, 0.52);
      rig.add(rotor);
      anim.rotParts.push({ mesh: rotor, axis: "y", speed: 6.5 });
      const goldBar = createProductCrate(0xfde047, true);
      goldBar.position.set(0, 0.38, 0.88);
      rig.add(goldBar);
    } else if (def.id === "crystal_processor") {
      // Precision White/Titanium Pedestal + Twin Gimballed Laser Rings + Hovering Prism
      const ped = new THREE.Mesh(new THREE.CylinderGeometry(0.82, 0.95, 0.55, 16), metalMat(0xe2e8f0, { roughness: 0.25 }));
      ped.position.y = 0.38;
      rig.add(ped);
      const ringA = new THREE.Mesh(new THREE.TorusGeometry(0.75, 0.055, 10, 28), metalMat(0x22d3ee, { emissive: 0x06b6d4, emissiveIntensity: 0.6 }));
      ringA.position.y = 1.4;
      rig.add(ringA);
      const ringB = ringA.clone();
      ringB.scale.setScalar(0.78);
      ringB.rotation.y = Math.PI / 2;
      rig.add(ringB);
      anim.rotParts.push({ mesh: ringA, axis: "x", speed: 2.6 });
      anim.rotParts.push({ mesh: ringB, axis: "y", speed: 3.4 });
      const gem = new THREE.Mesh(crystalPrismGeo, pbrMat(0x67e8f9, { emissive: 0x22d3ee, emissiveIntensity: 0.75, roughness: 0.12, metalness: 0.2 }));
      gem.scale.setScalar(0.65);
      gem.position.y = 1.4;
      rig.add(gem);
      anim.rotParts.push({ mesh: gem, axis: "y", speed: 2.0 });
    } else {
      // Industrial Factory: Bay Hull + Dual Articulated Robotic Assembly Arms
      const bay = new THREE.Mesh(makeRoundedBoxGeo(2.2, 1.15, 2.0, 0.07), metalMat(0x334155));
      bay.position.y = 0.68;
      bay.castShadow = true;
      rig.add(bay);
      const glowStrip = new THREE.Mesh(makeRoundedBoxGeo(2.24, 0.12, 2.04, 0.02), pbrMat(0xe879f9, { emissive: 0xe879f9, emissiveIntensity: 0.9 }));
      glowStrip.position.y = 1.18;
      rig.add(glowStrip);
      [-0.55, 0.55].forEach((ax, ai) => {
        const armBase = new THREE.Group();
        armBase.position.set(ax, 1.32, 0);
        const upper = new THREE.Mesh(makeCapsuleGeo(0.08, 0.72, 8), metalMat(0xfbbf24));
        upper.position.y = 0.35;
        upper.rotation.z = ai === 0 ? -0.45 : 0.45;
        armBase.add(upper);
        const head = new THREE.Mesh(octaGeo, metalMat(0x22d3ee, { emissive: 0x06b6d4, emissiveIntensity: 0.6 }));
        head.scale.setScalar(0.14);
        head.position.set(ai === 0 ? 0.25 : -0.25, 0.7, 0);
        armBase.add(head);
        rig.add(armBase);
        anim.rotParts.push({ mesh: armBase, axis: "y", speed: ai === 0 ? 2.4 : -2.4 });
      });
      anim.smokeOrigin = { lx: 0, ly: 1.9, lz: -0.6, col: 0xe879f9 };
    }

    worldGroup.add(grp);
    worldMachines[def.id] = { def, group: grp, pad, rig, statusLed, anim, crateT: 0 };
  }
  MACHINE_DEFS.forEach(createMachineModel);

  // =========================================================================
  // 8B. DETAILED 3D FLEET VEHICLES & CARGO CONTAINERS
  // =========================================================================
  const worldFleet = {};

  // 1. Patrolling Industrial Forklift (moves along Forest Logistics Lane x=9.6, z=-2..4)
  (function buildForklift() {
    const grp = new THREE.Group();
    grp.position.set(9.4, 0, -1.5);
    grp.visible = false;
    addContactShadow(grp, 1.35);

    const chassis = new THREE.Mesh(makeRoundedBoxGeo(1.05, 0.58, 1.45, 0.08), pbrMat(0xfbbf24, { roughness: 0.38, metalness: 0.35 }));
    chassis.position.y = 0.52;
    chassis.castShadow = true;
    grp.add(chassis);
    const roof = new THREE.Mesh(makeRoundedBoxGeo(0.85, 0.08, 0.95, 0.02), metalMat(0x1e293b));
    roof.position.set(0, 1.45, -0.1);
    grp.add(roof);
    [[-0.4, -0.5], [0.4, -0.5], [-0.4, 0.32], [0.4, 0.32]].forEach(([px, pz]) => {
      const post = new THREE.Mesh(makeCapsuleGeo(0.04, 0.75, 6), metalMat(0x1e293b));
      post.position.set(px, 1.08, pz);
      grp.add(post);
    });
    const mast = new THREE.Mesh(makeRoundedBoxGeo(0.75, 1.55, 0.14, 0.02), metalMat(0x334155));
    mast.position.set(0, 1.0, 0.85);
    grp.add(mast);
    const forkGrp = new THREE.Group();
    forkGrp.position.set(0, 0.35, 1.15);
    [-0.24, 0.24].forEach((fx) => {
      const tine = new THREE.Mesh(makeRoundedBoxGeo(0.1, 0.06, 0.78, 0.01), metalMat(0x94a3b8));
      tine.position.set(fx, 0, 0.25);
      forkGrp.add(tine);
    });
    const cargo = createProductCrate(0xd97706, false);
    cargo.scale.setScalar(1.2);
    cargo.position.set(0, 0.24, 0.32);
    forkGrp.add(cargo);
    grp.add(forkGrp);

    for (const [wx, wz] of [[-0.56, -0.48], [0.56, -0.48], [-0.56, 0.48], [0.56, 0.48]]) {
      const w = createWheelMesh(0.24, 0.18);
      w.position.set(wx, 0.24, wz);
      grp.add(w);
    }
    worldGroup.add(grp);
    worldFleet.forklift = { group: grp, forkGrp };
  })();

  // 2. Automated Mining Cart on Railroad Tracks (Forest-Mountain Quarry Rail)
  (function buildMiningCart() {
    const trackGrp = new THREE.Group();
    trackGrp.position.set(-5.8, 0, -9.2);
    trackGrp.visible = false;

    for (let si = -2.8; si <= 2.8; si += 0.7) {
      const tie = new THREE.Mesh(makeRoundedBoxGeo(0.24, 0.08, 1.35, 0.02), pbrMat(0x78350f, { map: texWoodPlank }));
      tie.position.set(si, 0.04, 0);
      trackGrp.add(tie);
    }
    [-0.45, 0.45].forEach((rz) => {
      const rail = new THREE.Mesh(makeRoundedBoxGeo(6.0, 0.08, 0.08, 0.01), metalMat(0x94a3b8));
      rail.position.set(0, 0.1, rz);
      trackGrp.add(rail);
    });

    const cart = new THREE.Group();
    cart.position.y = 0.14;
    const tub = new THREE.Mesh(makeRoundedBoxGeo(1.35, 0.65, 0.95, 0.08), metalMat(0x475569, { map: texBrushedMetal }));
    tub.position.y = 0.52;
    tub.castShadow = true;
    cart.add(tub);
    [[-0.25, 0xfbbf24], [0.22, 0xcbd5e1], [0, 0x22d3ee]].forEach(([ox, oCol], oi) => {
      const ore = new THREE.Mesh(makeOrganicGeo(0.24, 1, 0.22, oi), metalMat(oCol, { roughness: 0.25 }));
      ore.position.set(ox, 0.88, (oi - 1) * 0.12);
      cart.add(ore);
    });
    [[-0.45, -0.45], [0.45, -0.45], [-0.45, 0.45], [0.45, 0.45]].forEach(([wx, wz]) => {
      const wheel = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.18, 0.1, 12), metalMat(0x1e293b));
      wheel.rotation.x = Math.PI / 2;
      wheel.position.set(wx, 0.18, wz);
      cart.add(wheel);
    });
    trackGrp.add(cart);
    worldGroup.add(trackGrp);
    worldFleet.mining_cart = { group: trackGrp, cart };
  })();

  // 3. Heavy Delivery Truck Parked at Trading Post Loading Bay
  (function buildDeliveryTruck() {
    const grp = new THREE.Group();
    grp.position.set(-9.2, 0, 7.6);
    grp.rotation.y = 0.62;
    grp.visible = false;
    addContactShadow(grp, 2.3);

    const chassis = new THREE.Mesh(makeRoundedBoxGeo(1.45, 0.26, 3.7, 0.05), metalMat(0x1e293b));
    chassis.position.y = 0.42;
    grp.add(chassis);
    const cargoBox = new THREE.Mesh(makeRoundedBoxGeo(1.56, 1.35, 2.4, 0.08), pbrMat(0x0284c7, { map: texBrushedMetal, roughness: 0.42 }));
    cargoBox.position.set(0, 1.22, -0.58);
    cargoBox.castShadow = true;
    grp.add(cargoBox);
    const cab = new THREE.Mesh(makeRoundedBoxGeo(1.42, 1.15, 1.05, 0.1), pbrMat(0xf8fafc, { roughness: 0.32, metalness: 0.2 }));
    cab.position.set(0, 1.1, 1.22);
    cab.castShadow = true;
    grp.add(cab);
    const windshield = new THREE.Mesh(makeRoundedBoxGeo(1.28, 0.46, 0.14, 0.02), metalMat(0x38bdf8, { roughness: 0.1, metalness: 0.9 }));
    windshield.position.set(0, 1.28, 1.72);
    grp.add(windshield);
    const beacon = new THREE.Mesh(sphereGeo, pbrMat(0xf59e0b, { emissive: 0xf59e0b, emissiveIntensity: 1.2 }));
    beacon.scale.setScalar(0.12);
    beacon.position.set(0, 1.78, 1.22);
    grp.add(beacon);

    for (const [wx, wz] of [[-0.78, -1.3], [0.78, -1.3], [-0.78, -0.35], [0.78, -0.35], [-0.78, 1.22], [0.78, 1.22]]) {
      const w = createWheelMesh(0.32, 0.22);
      w.position.set(wx, 0.32, wz);
      grp.add(w);
    }
    worldGroup.add(grp);
    worldFleet.delivery_truck = { group: grp, beacon };
  })();

  // 4. Heavy Articulated Excavator Rig
  (function buildExcavator() {
    const grp = new THREE.Group();
    grp.position.set(-11.8, 0, -11.4);
    grp.rotation.y = 0.78;
    grp.visible = false;
    addContactShadow(grp, 2.0);

    // Crawler Tracks
    [-0.72, 0.72].forEach((tx) => {
      const track = new THREE.Mesh(makeRoundedBoxGeo(0.42, 0.44, 2.15, 0.18), pbrMat(0x1e293b, { roughness: 0.9 }));
      track.position.set(tx, 0.24, 0);
      track.castShadow = true;
      grp.add(track);
    });

    const turret = new THREE.Group();
    turret.position.y = 0.58;
    grp.add(turret);

    const engineBay = new THREE.Mesh(makeRoundedBoxGeo(1.42, 0.85, 1.55, 0.1), pbrMat(0x10b981, { roughness: 0.38, metalness: 0.35 }));
    engineBay.position.set(0, 0.45, -0.22);
    engineBay.castShadow = true;
    turret.add(engineBay);

    const cab = new THREE.Mesh(makeRoundedBoxGeo(0.68, 0.98, 0.92, 0.08), metalMat(0x0f172a, { roughness: 0.25 }));
    cab.position.set(-0.42, 0.65, 0.25);
    turret.add(cab);

    const boomPivot = new THREE.Group();
    boomPivot.position.set(0.28, 0.65, 0.42);
    turret.add(boomPivot);

    const boom = new THREE.Mesh(makeRoundedBoxGeo(0.24, 1.55, 0.28, 0.04), pbrMat(0xfbbf24, { roughness: 0.35, metalness: 0.4 }));
    boom.position.set(0, 0.68, 0.45);
    boom.rotation.x = 0.55;
    boom.castShadow = true;
    boomPivot.add(boom);

    const stick = new THREE.Mesh(makeRoundedBoxGeo(0.2, 1.15, 0.24, 0.04), metalMat(0xf59e0b));
    stick.position.set(0, 0.78, 1.28);
    stick.rotation.x = -0.78;
    boomPivot.add(stick);

    const bucket = new THREE.Mesh(new THREE.CylinderGeometry(0.32, 0.36, 0.62, 10, 1, false, 0, Math.PI * 1.25), metalMat(0x475569));
    bucket.rotation.z = Math.PI / 2;
    bucket.position.set(0, 0.18, 1.65);
    boomPivot.add(bucket);

    worldGroup.add(grp);
    worldFleet.excavator = { group: grp, turret, boomPivot };
  })();

  // 5. Stacked Corrugated Cargo Containers (beside Warehouse x=11.8, z=-6.5)
  (function buildContainers() {
    const grp = new THREE.Group();
    grp.position.set(11.6, 0, -6.5);
    grp.visible = false;
    addContactShadow(grp, 2.2);

    const containers = [];
    const cols = [0x0284c7, 0xd97706, 0x10b981];
    for (let ci = 0; ci < 3; ci++) {
      const c = new THREE.Mesh(
        makeRoundedBoxGeo(2.6, 1.08, 1.32, 0.04),
        pbrMat(cols[ci], { map: texBrushedMetal, roughness: 0.48, metalness: 0.55 })
      );
      c.position.set(ci === 2 ? 0.1 : 0, 0.56 + (ci % 2) * 1.1, ci === 2 ? 1.45 : 0);
      c.castShadow = true;
      c.visible = false;
      grp.add(c);
      containers.push(c);
    }
    worldGroup.add(grp);
    worldFleet.storage_container = { group: grp, containers };
  })();

  // =========================================================================
  // 9. ORGANIC BRANCHING TREES & NATURAL STRATIFIED MINERAL NODES
  // =========================================================================
  const resourceNodes = [];
  const flyingDrops = [];
  const particles = [];

  function createNodeMesh(kind, biomeId) {
    const grp = new THREE.Group();
    addContactShadow(grp, kind === "wood" ? 1.35 : 1.1);

    const visualRoot = new THREE.Group();
    grp.add(visualRoot);
    let crackMesh = null;

    if (kind === "wood") {
      // Flared Buttress Roots + Tapered Bark Trunk + Forked Limbs + Sculpted Canopy Clusters
      const trunkPts = [
        new THREE.Vector2(0.46, 0.0),
        new THREE.Vector2(0.28, 0.25),
        new THREE.Vector2(0.21, 0.85),
        new THREE.Vector2(0.15, 1.65),
        new THREE.Vector2(0.08, 2.3)
      ];
      const trunkGeo = new THREE.LatheGeometry(trunkPts, 10);
      const trunk = new THREE.Mesh(trunkGeo, pbrMat(0xffffff, { map: texBark, bumpMap: texBarkBump, bumpScale: 0.08, roughness: 0.88 }));
      trunk.castShadow = true;
      visualRoot.add(trunk);

      // 3 Natural Angled Tree Boughs
      for (let bi = 0; bi < 3; bi++) {
        const bAng = bi * (Math.PI * 2 / 3) + 0.4;
        const bough = new THREE.Mesh(makeCapsuleGeo(0.065, 0.72, 7), pbrMat(0xffffff, { map: texBark, roughness: 0.9 }));
        bough.position.set(Math.cos(bAng) * 0.26, 1.32 + bi * 0.16, Math.sin(bAng) * 0.26);
        bough.rotation.z = -Math.cos(bAng) * 0.72;
        bough.rotation.x = Math.sin(bAng) * 0.72;
        bough.castShadow = true;
        visualRoot.add(bough);
      }

      // Axe Chop Wedge Notch
      crackMesh = new THREE.Mesh(
        new THREE.ConeGeometry(0.28, 0.22, 4),
        pbrMat(0xd97706, { roughness: 0.65 })
      );
      crackMesh.rotation.z = Math.PI / 2;
      crackMesh.position.set(0.18, 0.72, 0);
      crackMesh.visible = false;
      visualRoot.add(crackMesh);

      const leafA = biomeId === "frozen" ? 0x99f6e4 : (biomeId === "jungle" ? 0x059669 : 0x22a84f);
      const leafB = biomeId === "frozen" ? 0xcffafe : (biomeId === "jungle" ? 0x10b981 : 0x34d36a);
      const clusters = [
        [0, 2.15, 0, 1.12, 0.85, leafA],
        [-0.58, 1.85, 0.42, 0.78, 0.62, leafB],
        [0.62, 1.9, -0.35, 0.76, 0.6, leafA],
        [0.12, 2.68, 0.22, 0.74, 0.68, leafB],
        [0, 3.12, 0, 0.52, 0.55, leafA]
      ];
      clusters.forEach(([lx, ly, lz, sx, sy, col], ci) => {
        const foliage = new THREE.Mesh(
          makeOrganicGeo(1, 1, 0.25, ci + 1.5),
          pbrMat(col, { map: texFoliage, roughness: 0.76 })
        );
        foliage.scale.set(sx, sy, sx);
        foliage.position.set(lx, ly, lz);
        foliage.castShadow = true;
        visualRoot.add(foliage);
      });
    } else if (kind === "crystal" || kind === "rare") {
      const col = RESOURCES[kind].color;
      // Stone Cluster Base + Faceted Hexagonal Crystal Spires
      const baseBed = new THREE.Mesh(organicRockGeoA, pbrMat(0x334155, { map: texRock, roughness: 0.85 }));
      baseBed.scale.set(0.9, 0.34, 0.85);
      baseBed.position.y = 0.22;
      visualRoot.add(baseBed);

      const shardMat = new THREE.MeshStandardMaterial({
        color: col,
        emissive: col,
        emissiveIntensity: 0.45,
        roughness: 0.12,
        metalness: 0.25,
        envMapIntensity: 1.5
      });
      [[0, 0.82, 0, 1.0, 1.15, 0], [-0.36, 0.62, 0.24, 0.68, 0.82, 0.35], [0.38, 0.58, -0.2, 0.65, 0.76, -0.32]].forEach(([sx, sy, sz, rx, ry, rz]) => {
        const sp = new THREE.Mesh(crystalPrismGeo, shardMat);
        sp.scale.set(rx, ry, rx);
        sp.position.set(sx, sy, sz);
        sp.rotation.z = rz;
        sp.castShadow = true;
        visualRoot.add(sp);
      });
      crackMesh = new THREE.Mesh(new THREE.RingGeometry(0.45, 0.68, 6), new THREE.MeshBasicMaterial({ color: 0xffffff, side: THREE.DoubleSide }));
      crackMesh.rotation.x = -Math.PI / 2;
      crackMesh.position.y = 0.35;
      crackMesh.visible = false;
      visualRoot.add(crackMesh);
    } else {
      // Natural Irregular Stratified Boulder + Ore Seam Deposits
      const mainGeo = kind === "stone" ? organicRockGeoA : organicRockGeoB;
      const baseRock = new THREE.Mesh(
        mainGeo,
        pbrMat(kind === "coal" ? 0x3f3f46 : 0x7c8a99, { map: texRock, roughness: 0.88 })
      );
      baseRock.scale.set(0.95, 0.72, 0.88);
      baseRock.position.y = 0.52;
      baseRock.castShadow = true;
      visualRoot.add(baseRock);

      const sideRock = new THREE.Mesh(organicRockGeoA, pbrMat(0x52606d, { map: texRock, roughness: 0.9 }));
      sideRock.scale.set(0.5, 0.38, 0.46);
      sideRock.position.set(0.62, 0.26, 0.32);
      sideRock.castShadow = true;
      visualRoot.add(sideRock);

      crackMesh = new THREE.Mesh(new THREE.BoxGeometry(1.15, 0.08, 0.14), pbrMat(0x0f172a));
      crackMesh.position.y = 0.62;
      crackMesh.rotation.z = 0.35;
      crackMesh.visible = false;
      visualRoot.add(crackMesh);

      if (kind !== "stone") {
        const oreCol = RESOURCES[kind].color;
        const oreMat = metalMat(oreCol, {
          emissive: kind === "gold" ? 0xb45309 : 0x000000,
          emissiveIntensity: 0.28,
          roughness: 0.26,
          metalness: 0.88
        });
        for (let i = 0; i < 5; i++) {
          const nugget = new THREE.Mesh(makeOrganicGeo(0.22, 0, 0.26, i + 2), oreMat);
          const ang = i * 1.26;
          nugget.position.set(Math.cos(ang) * 0.56, 0.56 + (i % 2) * 0.22, Math.sin(ang) * 0.54);
          nugget.rotation.set(0.4, ang, 0.3);
          visualRoot.add(nugget);
        }
      }
    }

    return { grp, visualRoot, crackMesh };
  }

  function populateBiomeNodes() {
    const specs = [
      { biome: "forest", kind: "wood", x: -5.2, z: -0.8, hp: 3 },
      { biome: "forest", kind: "wood", x: 5.0, z: 1.4, hp: 3 },
      { biome: "forest", kind: "stone", x: -2.6, z: -5.0, hp: 3 },
      { biome: "forest", kind: "stone", x: 4.0, z: -4.8, hp: 3 },
      { biome: "forest", kind: "wood", x: -10, z: 9, hp: 3 },
      { biome: "forest", kind: "wood", x: -12, z: 3, hp: 3 },
      { biome: "forest", kind: "wood", x: -11, z: -11, hp: 3 },
      { biome: "forest", kind: "wood", x: 10, z: -10, hp: 3 },
      { biome: "forest", kind: "wood", x: 12, z: 11, hp: 3 },
      { biome: "forest", kind: "wood", x: 2, z: 12, hp: 3 },
      { biome: "forest", kind: "wood", x: -7.2, z: -9.5, hp: 3 },
      { biome: "forest", kind: "wood", x: 9.8, z: -2.5, hp: 3 },
      { biome: "forest", kind: "stone", x: -4, z: -11, hp: 3 },
      { biome: "forest", kind: "stone", x: 11, z: 1, hp: 3 },
      { biome: "forest", kind: "stone", x: -12, z: -2, hp: 3 },

      { biome: "mountains", kind: "stone", x: -10, z: -40, hp: 4 },
      { biome: "mountains", kind: "coal",  x: -4,  z: -43, hp: 4 },
      { biome: "mountains", kind: "coal",  x: 11,  z: -36, hp: 4 },
      { biome: "mountains", kind: "iron",  x: 4,   z: -44, hp: 5 },
      { biome: "mountains", kind: "iron",  x: -11, z: -34, hp: 5 },
      { biome: "mountains", kind: "iron",  x: 10,  z: -24, hp: 5 },
      { biome: "mountains", kind: "wood",  x: -6,  z: -22, hp: 3 },

      { biome: "desert", kind: "copper", x: 26, z: -9,  hp: 5 },
      { biome: "desert", kind: "copper", x: 42, z: -6,  hp: 5 },
      { biome: "desert", kind: "copper", x: 25, z: 9,   hp: 5 },
      { biome: "desert", kind: "gold",   x: 43, z: 8,   hp: 7 },
      { biome: "desert", kind: "gold",   x: 38, z: -11, hp: 7 },
      { biome: "desert", kind: "stone",  x: 30, z: 12,  hp: 4 },

      { biome: "frozen", kind: "crystal", x: -42, z: -8, hp: 8 },
      { biome: "frozen", kind: "crystal", x: -26, z: -10, hp: 8 },
      { biome: "frozen", kind: "crystal", x: -43, z: 9,  hp: 8 },
      { biome: "frozen", kind: "iron",    x: -25, z: 10, hp: 6 },
      { biome: "frozen", kind: "wood",    x: -36, z: -11, hp: 4 },
      { biome: "frozen", kind: "wood",    x: -44, z: 0,  hp: 4 },

      { biome: "volcanic", kind: "coal", x: -10, z: 26, hp: 6 },
      { biome: "volcanic", kind: "gold", x: 11,  z: 25, hp: 8 },
      { biome: "volcanic", kind: "gold", x: -11, z: 42, hp: 8 },
      { biome: "volcanic", kind: "rare", x: 0,   z: 44, hp: 10 },
      { biome: "volcanic", kind: "rare", x: 11,  z: 43, hp: 10 },

      { biome: "jungle", kind: "wood",    x: 24, z: -42, hp: 5 },
      { biome: "jungle", kind: "wood",    x: 44, z: -26, hp: 5 },
      { biome: "jungle", kind: "crystal", x: 24, z: -24, hp: 9 },
      { biome: "jungle", kind: "rare",    x: 43, z: -42, hp: 12 },
      { biome: "jungle", kind: "rare",    x: 34, z: -44, hp: 12 }
    ];

    specs.forEach((sp, i) => {
      const built = createNodeMesh(sp.kind, sp.biome);
      built.grp.position.set(sp.x, 0, sp.z);
      built.grp.rotation.y = (i * 1.37) % (Math.PI * 2);
      worldGroup.add(built.grp);
      resourceNodes.push({
        id: i,
        biome: sp.biome,
        kind: sp.kind,
        x: sp.x,
        z: sp.z,
        maxHp: sp.hp,
        hp: sp.hp,
        respawnTimer: 0,
        bounceTimer: 0,
        fallTimer: 0,
        fallTotal: 0.48,
        fallDirZ: 1,
        mesh: built.grp,
        visualRoot: built.visualRoot,
        crackMesh: built.crackMesh
      });
    });
  }
  populateBiomeNodes();

  // =========================================================================
  // 10. STYLIZED SPIKY-HAIRED LUMBERJACK HERO + 8-TIER EXTRUDED TOOLS
  // =========================================================================
  const playerGroup = new THREE.Group();
  playerGroup.position.set(0, 0, 2);
  scene.add(playerGroup);
  addContactShadow(playerGroup, 0.8);

  const playerBodyPivot = new THREE.Group();
  playerGroup.add(playerBodyPivot);

  // Buffalo-Plaid Flannel Shirt Torso (softly contoured toy-hero proportions)
  const torsoMat = pbrMat(0xffffff, { map: texPlaid, roughness: 0.62, metalness: 0.04 });
  const torsoGeo = makeRoundedBoxGeo(0.76, 0.84, 0.46, 0.11);
  const torso = new THREE.Mesh(torsoGeo, torsoMat);
  torso.position.y = 1.08;
  torso.castShadow = true;
  playerBodyPivot.add(torso);

  // Shirt Collar V-Opening + Undershirt Neck
  const neckMesh = new THREE.Mesh(new THREE.CylinderGeometry(0.14, 0.16, 0.2, 12), pbrMat(0xffd97d, { roughness: 0.5 }));
  neckMesh.position.y = 1.54;
  playerBodyPivot.add(neckMesh);

  // Dark Brown Leather Belt + Golden Square Frame Buckle
  const belt = new THREE.Mesh(makeRoundedBoxGeo(0.8, 0.14, 0.5, 0.04), pbrMat(0x452212, { roughness: 0.55 }));
  belt.position.y = 0.72;
  playerBodyPivot.add(belt);
  const buckle = new THREE.Mesh(makeRoundedBoxGeo(0.18, 0.15, 0.07, 0.025), metalMat(0xf59e0b, { roughness: 0.25 }));
  buckle.position.set(0, 0.72, 0.24);
  playerBodyPivot.add(buckle);

  // Smooth Rounded Cylindrical Toy-Hero Head, Ears, Pill Eyes & Happy Smile
  const skinMat = pbrMat(0xffdb8a, { roughness: 0.48, metalness: 0.02 });
  const headPts = [
    new THREE.Vector2(0.01, -0.31),
    new THREE.Vector2(0.24, -0.3),
    new THREE.Vector2(0.32, -0.22),
    new THREE.Vector2(0.32, 0.22),
    new THREE.Vector2(0.24, 0.31),
    new THREE.Vector2(0.01, 0.32)
  ];
  const head = new THREE.Mesh(new THREE.LatheGeometry(headPts, 20), skinMat);
  head.position.y = 1.88;
  head.castShadow = true;
  playerBodyPivot.add(head);

  // Rounded Side Ears
  [-0.31, 0.31].forEach((ex) => {
    const ear = new THREE.Mesh(sphereGeo, skinMat);
    ear.scale.set(0.06, 0.09, 0.07);
    ear.position.set(ex, 1.84, 0.0);
    playerBodyPivot.add(ear);
  });

  // Expressive Dark Pill Eyes + Happy Curved Smile
  const eyeMat = pbrMat(0x111827, { roughness: 0.18 });
  [-0.1, 0.1].forEach((ex) => {
    const eye = new THREE.Mesh(makeCapsuleGeo(0.036, 0.08, 10), eyeMat);
    eye.position.set(ex, 1.91, 0.3);
    playerBodyPivot.add(eye);
  });
  const smileGeo = new THREE.TorusGeometry(0.1, 0.022, 8, 16, Math.PI * 0.85);
  const smile = new THREE.Mesh(smileGeo, eyeMat);
  smile.rotation.z = Math.PI * 1.075;
  smile.position.set(0, 1.77, 0.3);
  playerBodyPivot.add(smile);

  // Iconic Spiky Vibrant Orange Hair Crown
  const hairMat = pbrMat(0xf97316, { roughness: 0.38, metalness: 0.05 });
  const hairGroup = new THREE.Group();
  hairGroup.position.set(0, 2.08, -0.02);
  playerBodyPivot.add(hairGroup);

  const hairCap = new THREE.Mesh(sphereGeo, hairMat);
  hairCap.scale.set(0.34, 0.16, 0.34);
  hairCap.position.y = 0.04;
  hairCap.castShadow = true;
  hairGroup.add(hairCap);

  const backHair = new THREE.Mesh(makeRoundedBoxGeo(0.62, 0.32, 0.24, 0.08), hairMat);
  backHair.position.set(0, -0.08, -0.2);
  hairGroup.add(backHair);

  // 7 Swept Flame-Like Spikes matching reference silhouette
  const hairSpikes = [
    [-0.18, 0.18, 0.14, 0.14, 0.36, 0.35, -0.2],
    [0.0,   0.25, 0.16, 0.15, 0.44, 0.0,  -0.28],
    [0.18,  0.2,  0.14, 0.14, 0.38, -0.35, -0.2],
    [-0.27, 0.11, 0.02, 0.12, 0.3,  0.65, -0.15],
    [0.27,  0.11, 0.02, 0.12, 0.3,  -0.65, -0.15],
    [-0.11, 0.22, -0.08, 0.14, 0.4, 0.25, -0.45],
    [0.12,  0.22, -0.08, 0.14, 0.4, -0.25, -0.45]
  ];
  hairSpikes.forEach(([hx, hy, hz, r, h, rz, rx]) => {
    const spike = new THREE.Mesh(new THREE.ConeGeometry(r, h, 8), hairMat);
    spike.position.set(hx, hy, hz);
    spike.rotation.z = rz;
    spike.rotation.x = rx;
    spike.castShadow = true;
    hairGroup.add(spike);
  });

  const helmetCrest = new THREE.Mesh(octaGeo, metalMat(0x22d3ee, { emissive: 0x06b6d4, emissiveIntensity: 0.7 }));
  helmetCrest.scale.set(0.14, 0.2, 0.14);
  helmetCrest.position.set(0, 2.52, 0.08);
  helmetCrest.visible = false;
  playerBodyPivot.add(helmetCrest);

  // Armor Shoulder Pauldrons (unlocked at Steel+ tier)
  const pauldronMat = metalMat(0x64748b);
  const pauldronL = new THREE.Mesh(makeOrganicGeo(0.22, 1, 0.12, 1), pauldronMat);
  pauldronL.scale.set(1.1, 0.7, 1.12);
  pauldronL.position.set(-0.48, 1.46, 0);
  pauldronL.visible = false;
  playerBodyPivot.add(pauldronL);
  const pauldronR = pauldronL.clone();
  pauldronR.position.x = 0.48;
  playerBodyPivot.add(pauldronR);

  // Chainsaw/Power Pack Harness
  const powerPackMesh = new THREE.Mesh(makeRoundedBoxGeo(0.26, 0.34, 0.2, 0.04), metalMat(0xf97316));
  powerPackMesh.position.set(-0.34, 0.95, -0.2);
  powerPackMesh.visible = false;
  playerBodyPivot.add(powerPackMesh);

  const playerAuraRing = new THREE.Mesh(
    new THREE.RingGeometry(0.88, 1.08, 28),
    new THREE.MeshBasicMaterial({ color: 0xe879f9, side: THREE.DoubleSide, transparent: true, opacity: 0.65 })
  );
  playerAuraRing.rotation.x = -Math.PI / 2;
  playerAuraRing.position.y = 0.05;
  playerAuraRing.visible = false;
  playerGroup.add(playerAuraRing);

  // Charcoal Trousers + Chunky Tan-Orange Work Boots with Laces
  const legMat = pbrMat(0x2d2926, { roughness: 0.72 });
  const bootMat = pbrMat(0xf59e0b, { roughness: 0.46, metalness: 0.05 });
  const soleMat = pbrMat(0x452212, { roughness: 0.8 });
  function makeLeg(x) {
    const lg = new THREE.Group();
    lg.position.set(x, 0.66, 0);
    const limb = new THREE.Mesh(makeCapsuleGeo(0.12, 0.36, 10), legMat);
    limb.position.y = -0.22;
    limb.castShadow = true;
    lg.add(limb);

    const bootBody = new THREE.Mesh(makeRoundedBoxGeo(0.26, 0.2, 0.38, 0.07), bootMat);
    bootBody.position.set(0, -0.54, 0.06);
    bootBody.castShadow = true;
    lg.add(bootBody);
    const bootCuff = new THREE.Mesh(new THREE.CylinderGeometry(0.135, 0.145, 0.14, 12), bootMat);
    bootCuff.position.set(0, -0.42, 0);
    lg.add(bootCuff);
    const sole = new THREE.Mesh(makeRoundedBoxGeo(0.28, 0.055, 0.4, 0.02), soleMat);
    sole.position.set(0, -0.64, 0.06);
    lg.add(sole);
    const lace = new THREE.Mesh(makeRoundedBoxGeo(0.11, 0.08, 0.05, 0.01), soleMat);
    lace.position.set(0, -0.44, 0.14);
    lg.add(lace);

    playerBodyPivot.add(lg);
    return lg;
  }
  const legL = makeLeg(-0.18);
  const legR = makeLeg(0.18);

  // Articulated Arms with Flannel Sleeves, Brown Leather Wristbands & Rounded Hands
  const wristMat = pbrMat(0x452212, { roughness: 0.6 });
  function buildHeroArm(sideX) {
    const piv = new THREE.Group();
    piv.position.set(sideX, 1.4, 0);
    const sleeve = new THREE.Mesh(makeCapsuleGeo(0.115, 0.16, 10), torsoMat);
    sleeve.position.y = -0.1;
    sleeve.castShadow = true;
    piv.add(sleeve);
    const forearm = new THREE.Mesh(makeCapsuleGeo(0.092, 0.28, 10), skinMat);
    forearm.position.y = -0.3;
    forearm.castShadow = true;
    piv.add(forearm);
    const band = new THREE.Mesh(new THREE.CylinderGeometry(0.105, 0.105, 0.1, 12), wristMat);
    band.position.y = -0.39;
    piv.add(band);
    const hand = new THREE.Mesh(sphereGeo, skinMat);
    hand.scale.set(0.11, 0.11, 0.11);
    hand.position.y = -0.5;
    piv.add(hand);
    playerBodyPivot.add(piv);
    return piv;
  }
  const armL = buildHeroArm(-0.47);
  const armRPivot = buildHeroArm(0.47);

  const toolGroup = new THREE.Group();
  toolGroup.position.set(0, -0.52, 0.14);
  armRPivot.add(toolGroup);

  function buildAxeTierModel(tier) {
    const g = new THREE.Group();
    const haftCol = tier >= 7 ? 0x1e293b : (tier >= 5 ? 0x475569 : 0x78350f);
    const haft = new THREE.Mesh(
      makeCapsuleGeo(0.042, 0.85 + tier * 0.04, 8),
      pbrMat(haftCol, { map: tier < 5 ? texBark : texBrushedMetal, roughness: 0.65 })
    );
    haft.rotation.x = Math.PI / 2;
    haft.position.z = 0.3;
    g.add(haft);

    const headCol = TIER_COLORS[tier - 1];
    const bladeMat = tier >= 6
      ? metalMat(headCol, { emissive: headCol, emissiveIntensity: tier === 8 ? 0.6 : (tier === 7 ? 0.42 : 0.2), roughness: 0.18 })
      : (tier <= 2 ? pbrMat(headCol, { roughness: 0.72 }) : metalMat(headCol, { roughness: 0.3 }));

    // Curved Beveled Extruded Axe Blade Shape
    const bs = new THREE.Shape();
    const bw = 0.22 + tier * 0.025;
    const bh = 0.22 + tier * 0.032;
    bs.moveTo(-0.04, 0.06);
    bs.lineTo(bw, bh);
    bs.quadraticCurveTo(bw + 0.12, 0, bw, -bh * (tier >= 5 ? 0.85 : 0.35));
    bs.lineTo(-0.04, -0.06);
    bs.closePath();

    const bladeGeo = new THREE.ExtrudeGeometry(bs, {
      depth: 0.055,
      bevelEnabled: true,
      bevelThickness: 0.02,
      bevelSize: 0.02,
      bevelSegments: 2
    });
    bladeGeo.rotateY(Math.PI / 2);
    bladeGeo.rotateX(-Math.PI / 2);
    bladeGeo.translate(-0.0275, 0, 0);
    const blade = new THREE.Mesh(bladeGeo, bladeMat);
    blade.position.set(0, -0.04, 0.66 + tier * 0.02);
    blade.castShadow = true;
    g.add(blade);

    if (tier >= 5) {
      const twin = blade.clone();
      twin.rotation.z = Math.PI;
      twin.scale.set(0.85, 0.85, 0.85);
      twin.position.set(0, 0.04, 0.66 + tier * 0.02);
      g.add(twin);
    }
    if (tier === 8) {
      const core = new THREE.Mesh(octaGeo, pbrMat(0xfde047, { emissive: 0xfde047, emissiveIntensity: 1.2 }));
      core.scale.setScalar(0.11);
      core.position.set(0, 0.02, 0.9);
      g.add(core);
    }
    g.visible = false;
    toolGroup.add(g);
    return g;
  }

  function buildPickTierModel(tier) {
    const g = new THREE.Group();
    const haftCol = tier >= 7 ? 0x1e293b : (tier >= 5 ? 0x475569 : 0x78350f);
    const haft = new THREE.Mesh(
      makeCapsuleGeo(0.042, 0.85 + tier * 0.04, 8),
      pbrMat(haftCol, { map: tier < 5 ? texBark : texBrushedMetal })
    );
    haft.rotation.x = Math.PI / 2;
    haft.position.z = 0.3;
    g.add(haft);

    const headCol = TIER_COLORS[tier - 1];
    const headMat = tier >= 6
      ? metalMat(headCol, { emissive: headCol, emissiveIntensity: tier === 8 ? 0.6 : 0.32, roughness: 0.18 })
      : metalMat(headCol, { roughness: tier <= 2 ? 0.72 : 0.32, metalness: tier <= 2 ? 0.15 : 0.85 });

    // Curved Swept Pickaxe Crescent Arc
    const ps = new THREE.Shape();
    const span = 0.38 + tier * 0.032;
    ps.moveTo(-span, -0.09);
    ps.quadraticCurveTo(0, 0.14, span, -0.09);
    ps.quadraticCurveTo(0, 0.02, -span, -0.09);
    const pickGeo = new THREE.ExtrudeGeometry(ps, {
      depth: 0.07,
      bevelEnabled: true,
      bevelThickness: 0.02,
      bevelSize: 0.02,
      bevelSegments: 2
    });
    pickGeo.center();
    pickGeo.rotateZ(Math.PI / 2);
    pickGeo.rotateY(Math.PI / 2);
    const headMesh = new THREE.Mesh(pickGeo, headMat);
    headMesh.position.set(0, 0.0, 0.7 + tier * 0.02);
    headMesh.castShadow = true;
    g.add(headMesh);

    const collar = new THREE.Mesh(makeRoundedBoxGeo(0.16, 0.16, 0.18, 0.03), metalMat(0x1e293b));
    collar.position.set(0, 0.02, 0.7 + tier * 0.02);
    g.add(collar);

    g.visible = false;
    toolGroup.add(g);
    return g;
  }

  const axeTierModels = [1, 2, 3, 4, 5, 6, 7, 8].map(buildAxeTierModel);
  const pickTierModels = [1, 2, 3, 4, 5, 6, 7, 8].map(buildPickTierModel);
  let activeToolKind = "axe";

  function updateActiveToolMesh() {
    const axeIdx = Math.min(Math.max(state.axeLevel, 1), 8) - 1;
    const pickIdx = Math.min(Math.max(state.pickaxeLevel, 1), 8) - 1;
    axeTierModels.forEach((m, i) => { m.visible = (activeToolKind === "axe" && i === axeIdx); });
    pickTierModels.forEach((m, i) => { m.visible = (activeToolKind === "pick" && i === pickIdx); });
  }

  // Sculpted Expedition Backpack + Rolled Bedroll + Side Canisters
  const backpackMat = pbrMat(0x92400e, { roughness: 0.55 });
  const backpackMesh = new THREE.Mesh(makeRoundedBoxGeo(0.56, 0.68, 0.32, 0.08), backpackMat);
  backpackMesh.position.set(0, 1.08, -0.36);
  backpackMesh.castShadow = true;
  playerBodyPivot.add(backpackMesh);

  const bedroll = new THREE.Mesh(new THREE.CylinderGeometry(0.1, 0.1, 0.52, 10), pbrMat(0xfef0c7, { roughness: 0.75 }));
  bedroll.rotation.z = Math.PI / 2;
  bedroll.position.set(0, 1.44, -0.36);
  playerBodyPivot.add(bedroll);

  const bagPodMat = metalMat(0xcbd5e1);
  const bagPodL = new THREE.Mesh(makeCapsuleGeo(0.09, 0.34, 8), bagPodMat);
  bagPodL.position.set(-0.36, 1.06, -0.4);
  bagPodL.visible = false;
  playerBodyPivot.add(bagPodL);
  const bagPodR = bagPodL.clone();
  bagPodR.position.x = 0.36;
  bagPodR.visible = false;
  playerBodyPivot.add(bagPodR);

  const bagCoreMat = pbrMat(0x22d3ee, { emissive: 0x22d3ee, emissiveIntensity: 0.85, roughness: 0.15 });
  const bagCore = new THREE.Mesh(octaGeo, bagCoreMat);
  bagCore.scale.set(0.16, 0.2, 0.16);
  bagCore.position.set(0, 1.08, -0.62);
  bagCore.visible = false;
  playerBodyPivot.add(bagCore);

  const bagStackGroup = new THREE.Group();
  bagStackGroup.position.set(0, 1.56, -0.38);
  playerBodyPivot.add(bagStackGroup);

  function refreshCharacterVisuals() {
    const bagLvl = Math.min(Math.max(state.backpackLevel, 1), 8);
    const bagCol = TIER_COLORS[bagLvl - 1];
    backpackMat.color.setHex(bagCol);
    const s = 1 + (bagLvl - 1) * 0.08;
    backpackMesh.scale.set(s, s, s);
    backpackMesh.position.z = -0.35 - (bagLvl - 1) * 0.02;
    bagPodL.visible = bagLvl >= 4;
    bagPodR.visible = bagLvl >= 4;
    bagPodMat.color.setHex(bagLvl >= 7 ? 0x22d3ee : (bagLvl >= 6 ? 0xfbbf24 : 0xcbd5e1));
    bagCore.visible = bagLvl >= 7;
    if (bagLvl === 8) {
      bagCoreMat.color.setHex(0xe879f9);
      bagCoreMat.emissive.setHex(0xe879f9);
    } else {
      bagCoreMat.color.setHex(0x22d3ee);
      bagCoreMat.emissive.setHex(0x22d3ee);
    }

    const avgGear = (state.axeLevel + state.pickaxeLevel + state.backpackLevel + state.moveSpeedLevel + state.gatherSpeedLevel) / 5;
    pauldronL.visible = avgGear >= 4.2;
    pauldronR.visible = avgGear >= 4.2;
    helmetCrest.visible = avgGear >= 5.5;
    playerAuraRing.visible = avgGear >= 6.8;
    powerPackMesh.visible = getFleetLevel("chainsaw_drill") > 0;

    if (avgGear >= 7.0) {
      torsoMat.color.setHex(0xe9d5ff);
      hairMat.color.setHex(0xfde047);
      pauldronMat.color.setHex(0xe879f9);
    } else if (avgGear >= 5.8) {
      torsoMat.color.setHex(0xbae6fd);
      hairMat.color.setHex(0xfb923c);
      pauldronMat.color.setHex(0x38bdf8);
    } else if (avgGear >= 4.2) {
      torsoMat.color.setHex(0xfef3c7);
      hairMat.color.setHex(0xf97316);
      pauldronMat.color.setHex(0xfbbf24);
    } else {
      torsoMat.color.setHex(0xffffff);
      hairMat.color.setHex(0xf97316);
    }

    updateActiveToolMesh();

    while (bagStackGroup.children.length > 0) bagStackGroup.remove(bagStackGroup.children[0]);
    const keys = Object.keys(state.inventory).filter((k) => state.inventory[k] > 0);
    let idx = 0;
    for (const k of keys) {
      const count = Math.min(state.inventory[k], 4);
      const col = RESOURCES[k] ? RESOURCES[k].color : 0xffffff;
      for (let c = 0; c < count && idx < 10; c++) {
        const item = createProductCrate(col, k !== "wood");
        item.scale.setScalar(0.55);
        item.position.set((idx % 2 === 0 ? -0.12 : 0.12), Math.floor(idx / 2) * 0.14, 0);
        bagStackGroup.add(item);
        idx++;
      }
    }
  }

  // =========================================================================
  // 11. PARTICLES, DEBRIS CHUNKS & FLYING RESOURCE ARCS
  // =========================================================================
  function spawnBurst(x, y, z, colorHex, count, sizeScale) {
    const scBase = sizeScale || 0.16;
    for (let i = 0; i < (count || 6); i++) {
      const m = new THREE.Mesh(organicRockGeoA, pbrMat(colorHex, { roughness: 0.6 }));
      m.scale.setScalar(scBase);
      m.position.set(x, y, z);
      scene.add(m);
      const ang = Math.random() * Math.PI * 2;
      const spd = 2.0 + Math.random() * 3.8;
      particles.push({
        mesh: m,
        vx: Math.cos(ang) * spd,
        vy: 3.2 + Math.random() * 3.5,
        vz: Math.sin(ang) * spd,
        rotSpd: (Math.random() - 0.5) * 12,
        baseScale: scBase,
        age: 0,
        life: 0.62
      });
    }
  }

  function spawnFlyingDrop(fromPos, toTarget, colorHex) {
    const m = createProductCrate(colorHex, false);
    m.scale.setScalar(0.7);
    m.position.copy(fromPos);
    scene.add(m);
    flyingDrops.push({ mesh: m, start: fromPos.clone(), target: toTarget, t: 0, dur: 0.36 });
  }

  // =========================================================================
  // 12. VIRTUAL JOYSTICK (ONE-HAND CONTROL ONLY)
  // =========================================================================
  const joyZone = document.getElementById("joystick-zone");
  const joyBase = document.getElementById("joystick-base");
  const joyKnob = document.getElementById("joystick-knob");
  let activePointerId = null;
  let joyOriginX = 0;
  let joyOriginY = 0;
  let moveX = 0;
  let moveZ = 0;
  const maxJoyRadius = 46;

  function updateJoystickVisual(dx, dy) {
    const dist = Math.hypot(dx, dy);
    const clamped = Math.min(dist, maxJoyRadius);
    const ang = Math.atan2(dy, dx);
    const kx = Math.cos(ang) * clamped;
    const ky = Math.sin(ang) * clamped;
    joyKnob.style.transform = `translate3d(${kx}px, ${ky}px, 0)`;
    if (dist > 4) {
      const mag = clamped / maxJoyRadius;
      moveX = Math.cos(ang) * mag;
      moveZ = Math.sin(ang) * mag;
    } else {
      moveX = 0;
      moveZ = 0;
    }
  }

  joyZone.addEventListener("pointerdown", (e) => {
    ensureAudio();
    if (isMenuMode || activePointerId !== null) return;
    activePointerId = e.pointerId;
    joyZone.setPointerCapture(e.pointerId);
    const rect = joyBase.getBoundingClientRect();
    joyOriginX = rect.left + rect.width / 2;
    joyOriginY = rect.top + rect.height / 2;
    joyBase.classList.add("active");
    updateJoystickVisual(e.clientX - joyOriginX, e.clientY - joyOriginY);
  });

  joyZone.addEventListener("pointermove", (e) => {
    if (e.pointerId !== activePointerId) return;
    updateJoystickVisual(e.clientX - joyOriginX, e.clientY - joyOriginY);
  });

  function resetJoystick(e) {
    if (e && e.pointerId !== activePointerId) return;
    activePointerId = null;
    moveX = 0;
    moveZ = 0;
    joyKnob.style.transform = "translate3d(0px, 0px, 0)";
    joyBase.classList.remove("active");
  }
  joyZone.addEventListener("pointerup", resetJoystick);
  joyZone.addEventListener("pointercancel", resetJoystick);

  const keysDown = {};
  window.addEventListener("keydown", (e) => { keysDown[e.key.toLowerCase()] = true; });
  window.addEventListener("keyup", (e) => { keysDown[e.key.toLowerCase()] = false; });
  window.addEventListener("pointerdown", function () { ensureAudio(); }, { passive: true });

  // =========================================================================
  // 13. MOVEMENT COLLISION, BIOME BOUNDS & ANDROID BRIDGE
  // =========================================================================
  function isWalkable(x, z) {
    for (const b of BIOMES) {
      if (!isBiomeUnlocked(b.id)) continue;
      if (Math.abs(x - b.cx) <= 15.0 && Math.abs(z - b.cz) <= 15.0) return true;
    }
    if (isBiomeUnlocked("mountains") && Math.abs(x) <= 2.3 && z <= -14.5 && z >= -19.5) return true;
    if (isBiomeUnlocked("desert") && Math.abs(z) <= 2.3 && x >= 14.5 && x <= 19.5) return true;
    if (isBiomeUnlocked("frozen") && Math.abs(z) <= 2.3 && x <= -14.5 && x >= -19.5) return true;
    if (isBiomeUnlocked("volcanic") && Math.abs(x) <= 2.3 && z >= 14.5 && z <= 19.5) return true;
    if (isBiomeUnlocked("jungle") && Math.abs(x - 34) <= 2.3 && z <= -14.5 && z >= -19.5) return true;
    return false;
  }

  function currentBiomeAt(x, z) {
    let best = BIOMES[0];
    let bestDist = Infinity;
    for (const b of BIOMES) {
      const dist = Math.hypot(x - b.cx, z - b.cz);
      if (dist < bestDist) { bestDist = dist; best = b; }
    }
    return best;
  }

  function syncToAndroid(force) {
    const now = performance.now();
    if (!force && (!stateDirty || now - lastSyncTime < 250)) return;
    stateDirty = false;
    lastSyncTime = now;
    if (window.AndroidBridge && window.AndroidBridge.syncState) {
      try { window.AndroidBridge.syncState(JSON.stringify(state)); } catch (_) {}
    }
  }

  window.setMenuMode = function (inMenu) {
    isMenuMode = Boolean(inMenu);
    if (joyZoneEl) joyZoneEl.style.display = isMenuMode ? "none" : "block";
    if (floatLayer) floatLayer.style.display = isMenuMode ? "none" : "block";
    if (isMenuMode) resetJoystick({ pointerId: activePointerId });
    else ensureAudio();
  };

  window.applyGameState = function (incoming) {
    if (!incoming) return;
    state = Object.assign({}, state, incoming);
    applyGraphicsQuality();
    refreshCharacterVisuals();
    refreshWorldState();
  };

  window.teleportPlayer = function (x, z, biomeId) {
    playerGroup.position.set(x, 0, z);
    const found = BIOMES.find(function (b) { return b.id === biomeId; });
    showBanner("Traveled to " + (found ? found.name : "Region"));
  };

  window.triggerUpgradeCelebration = function () {
    const px = playerGroup.position.x, pz = playerGroup.position.z;
    spawnBurst(px, 1.35, pz, 0xfbbf24, 14, 0.2);
    spawnBurst(px, 1.65, pz, 0x22d3ee, 10, 0.16);
    playSfx("upgrade");
  };

  window.showNativeBanner = function (msg) {
    showBanner(msg);
    playSfx(msg && (msg.indexOf("Tier") !== -1 || msg.indexOf("Unlocked") !== -1) ? "upgrade" : "unlock");
  };

  window.flushSaveToAndroid = function () {
    stateDirty = true;
    syncToAndroid(true);
  };

  window.resetWorld = function () {
    playerGroup.position.set(0, 0, 2);
    resourceNodes.forEach((n) => {
      n.hp = n.maxHp;
      n.respawnTimer = 0;
      n.fallTimer = 0;
      n.visualRoot.rotation.set(0, 0, 0);
      if (n.crackMesh) n.crackMesh.visible = false;
      n.mesh.visible = true;
      n.mesh.scale.set(1, 1, 1);
    });
    refreshCharacterVisuals();
    refreshWorldState();
    showBanner("New Frontier Started!");
  };

  function refreshWorldState() {
    BIOMES.forEach((b) => {
      const g = biomeGates[b.id];
      if (!g) return;
      const unlocked = isBiomeUnlocked(b.id);
      g.barrier.visible = !unlocked;
      g.padRing.visible = !unlocked;
    });

    BUILDING_DEFS.forEach((def) => {
      const wb = worldBuildings[def.id];
      const lvl = state.buildingLevels[def.id] || 0;
      const workers = getBuildingWorkers(def.id);
      const lines = getBuildingLines(def.id);
      wb.struct.visible = lvl > 0;
      wb.ring.material.color.setHex(lvl > 0 ? 0xfbbf24 : 0x10b981);
      wb.lineModules.forEach((m, idx) => { m.visible = lvl > 0 && idx < lines; });
      wb.workerMeshes.forEach((m, idx) => { m.visible = lvl > 0 && idx < workers; });
    });

    // Update 10 Industrial Machines
    MACHINE_DEFS.forEach((def) => {
      const wm = worldMachines[def.id];
      if (!wm) return;
      const lvl = getMachineLevel(def.id);
      const running = isMachineRunning(def.id);
      const biomeOpen = isBiomeUnlocked(def.biome);
      wm.group.visible = biomeOpen || lvl > 0;
      wm.rig.visible = lvl > 0;
      wm.statusLed.material.color.setHex(running ? 0x22c55e : 0xef4444);
      wm.statusLed.material.emissive.setHex(running ? 0x22c55e : 0xef4444);
    });

    // Update Fleet Vehicles & Cargo Containers
    worldFleet.forklift.group.visible = getFleetLevel("forklift") > 0;
    worldFleet.mining_cart.group.visible = getFleetLevel("mining_cart") > 0;
    worldFleet.delivery_truck.group.visible = getFleetLevel("delivery_truck") > 0;
    worldFleet.excavator.group.visible = getFleetLevel("excavator") > 0;
    const cLvl = getFleetLevel("storage_container");
    worldFleet.storage_container.group.visible = cLvl > 0;
    worldFleet.storage_container.containers.forEach((c, idx) => {
      c.visible = (idx === 0 && cLvl >= 1) || (idx === 1 && cLvl >= 2) || (idx === 2 && cLvl >= 4);
    });
  }

  // =========================================================================
  // 14. MAIN GAME LOOP
  // =========================================================================
  let prevTime = performance.now();
  let swingCooldown = 0;
  let swingAnimTimer = 0;
  let sellCooldown = 0;
  let passiveAccum = 0;
  let fullBagWarnCooldown = 0;
  let activeBiomeId = "forest";
  let smokeTimer = 0;

  function tick(now) {
    requestAnimationFrame(tick);
    try {
      syncViewportSize();
      const dt = Math.min((now - prevTime) / 1000, 0.05);
      prevTime = now;

      tickBackgroundMusic(dt);

      // Gentle foliage wind & fireflies & cloud drift
      for (let ci = 0; ci < skyClouds.length; ci++) {
        skyClouds[ci].position.x += dt * (0.42 + ci * 0.08);
        if (skyClouds[ci].position.x > 52) skyClouds[ci].position.x = -52;
      }
      for (let gi = 0; gi < grassTufts.length; gi++) {
        grassTufts[gi].rotation.z = Math.sin(now * 0.0024 + gi) * 0.065;
      }
      for (let fi = 0; fi < fireflies.length; fi++) {
        const f = fireflies[fi];
        f.mesh.position.x = f.baseX + Math.sin(now * 0.001 * f.speed + f.phase) * 0.85;
        f.mesh.position.y = f.baseY + Math.sin(now * 0.0018 * f.speed + f.phase) * 0.35;
        f.mesh.position.z = f.baseZ + Math.cos(now * 0.0012 * f.speed + f.phase) * 0.85;
      }

      // 1. Player Movement (disabled in Home Menu mode)
      let inputX = isMenuMode ? 0 : moveX;
      let inputZ = isMenuMode ? 0 : moveZ;
      if (!isMenuMode && activePointerId === null) {
        let kx = 0, kz = 0;
        if (keysDown["arrowleft"] || keysDown["a"]) kx -= 1;
        if (keysDown["arrowright"] || keysDown["d"]) kx += 1;
        if (keysDown["arrowup"] || keysDown["w"]) kz -= 1;
        if (keysDown["arrowdown"] || keysDown["s"]) kz += 1;
        if (kx !== 0 || kz !== 0) {
          const kl = Math.hypot(kx, kz);
          inputX = kx / kl;
          inputZ = kz / kl;
        }
      }

      const speedBase = MOVE_SPEEDS[Math.min(state.moveSpeedLevel, 8) - 1];
      const speed = hasUniqueItem("golden_compass") ? speedBase * 1.15 : speedBase;
      const isMoving = Math.hypot(inputX, inputZ) > 0.05;
      if (playerAuraRing.visible) playerAuraRing.rotation.z += dt * 1.8;

      if (isMoving) {
        const nx = playerGroup.position.x + inputX * speed * dt;
        const nz = playerGroup.position.z + inputZ * speed * dt;
        if (isWalkable(nx, playerGroup.position.z)) playerGroup.position.x = nx;
        if (isWalkable(playerGroup.position.x, nz)) playerGroup.position.z = nz;

        const targetYaw = Math.atan2(inputX, inputZ);
        let diff = targetYaw - playerGroup.rotation.y;
        while (diff > Math.PI) diff -= Math.PI * 2;
        while (diff < -Math.PI) diff += Math.PI * 2;
        playerGroup.rotation.y += diff * Math.min(1, dt * 14);

        const gait = Math.sin(now * 0.015) * 0.58;
        legL.rotation.x = gait;
        legR.rotation.x = -gait;
        armL.rotation.x = -gait * 0.7;
        playerBodyPivot.position.y = Math.abs(Math.sin(now * 0.015)) * 0.085;
        playerBodyPivot.rotation.x = 0.07;
      } else {
        legL.rotation.x *= 0.8;
        legR.rotation.x *= 0.8;
        armL.rotation.x *= 0.8;
        playerBodyPivot.rotation.x *= 0.85;
        playerBodyPivot.position.y = Math.sin(now * 0.003) * 0.032;
        if (isMenuMode) {
          playerGroup.rotation.y = Math.sin(now * 0.0007) * 0.36;
        }
      }

      // 2. Smooth Third-Person Camera (or Scenic Home Menu Orbit)
      const px = playerGroup.position.x;
      const pz = playerGroup.position.z;
      const camLerp = 1 - Math.exp(-6.5 * dt);

      if (isMenuMode) {
        const orbitAng = Math.sin(now * 0.00035) * 0.42;
        camera.position.x += (px + Math.sin(orbitAng) * 5.6 - camera.position.x) * camLerp;
        camera.position.y += (4.0 - camera.position.y) * camLerp;
        camera.position.z += (pz + Math.cos(orbitAng) * 6.2 - camera.position.z) * camLerp;
        camera.lookAt(px, 1.35, pz - 1.1);
      } else {
        camera.position.x += (px - camera.position.x) * camLerp;
        camera.position.y += (14.2 - camera.position.y) * camLerp;
        camera.position.z += (pz + 13.2 - camera.position.z) * camLerp;
        camera.lookAt(px, 0.9, pz - 0.6);
      }

      sunLight.position.set(px + 16, 26, pz + 14);
      sunLight.target.position.set(px, 0, pz);
      skyDome.position.set(px, 0, pz);

      // 3. Biome Atmosphere Transition & Compose HUD Sync
      const bNow = currentBiomeAt(px, pz);
      if (bNow.id !== activeBiomeId) {
        activeBiomeId = bNow.id;
        if (window.AndroidBridge && window.AndroidBridge.onBiomeChanged) {
          try { window.AndroidBridge.onBiomeChanged(bNow.id); } catch (_) {}
        }
      }
      currentSkyColor.lerp(new THREE.Color(bNow.sky), dt * 2.5);
      currentFogColor.lerp(new THREE.Color(bNow.fog), dt * 2.5);
      skyUniforms.uTop.value.copy(currentSkyColor);
      skyUniforms.uHorizon.value.copy(currentFogColor);
      scene.fog.color.copy(currentFogColor);
      renderer.setClearColor(currentSkyColor, 1);

      // 4. Resource Nodes & Auto-Gathering (+ Chainsaw/Drill, Mining Cart & Excavator Bonuses)
      swingCooldown = Math.max(0, swingCooldown - dt);
      fullBagWarnCooldown = Math.max(0, fullBagWarnCooldown - dt);
      let nearestNode = null;
      let nearestDist = 2.65;

      for (const node of resourceNodes) {
        if (node.fallTimer > 0) {
          node.fallTimer -= dt;
          const u = 1 - Math.max(0, node.fallTimer) / node.fallTotal;
          node.visualRoot.rotation.z = node.fallDirZ * (u * u) * 1.48;
          if (node.fallTimer <= 0) {
            node.visualRoot.rotation.set(0, 0, 0);
            node.mesh.visible = false;
            node.respawnTimer = 4.2;
            const rDef = RESOURCES[node.kind];
            spawnBurst(node.x, 0.7, node.z, 0x22c55e, 7, 0.2);
            spawnBurst(node.x, 0.5, node.z, rDef.color, 6, 0.18);
          }
          continue;
        }
        if (node.respawnTimer > 0) {
          node.respawnTimer -= dt;
          if (node.respawnTimer <= 0) {
            node.hp = node.maxHp;
            node.visualRoot.rotation.set(0, 0, 0);
            if (node.crackMesh) node.crackMesh.visible = false;
            node.mesh.visible = true;
            node.mesh.scale.set(0.1, 0.1, 0.1);
          }
          continue;
        }
        if (node.mesh.scale.x < 1) {
          const ns = Math.min(1, node.mesh.scale.x + dt * 4);
          node.mesh.scale.set(ns, ns, ns);
        }
        if (node.bounceTimer > 0) {
          node.bounceTimer -= dt;
          const wobble = 1 + Math.sin(node.bounceTimer * 35) * 0.1;
          node.mesh.scale.set(wobble, 2 - wobble, wobble);
          if (node.kind === "wood") node.visualRoot.rotation.z = Math.sin(node.bounceTimer * 40) * 0.12;
        } else if (node.mesh.scale.x === 1) {
          node.mesh.scale.set(1, 1, 1);
          if (node.kind === "wood") node.visualRoot.rotation.z = Math.sin(now * 0.0016 + node.id) * 0.022;
        }

        if (isMenuMode || !isBiomeUnlocked(node.biome)) continue;
        const dist = Math.hypot(node.x - px, node.z - pz);
        if (dist < nearestDist) { nearestDist = dist; nearestNode = node; }
      }

      const chainsawLvl = getFleetLevel("chainsaw_drill");
      if (!isMenuMode && nearestNode && swingCooldown <= 0) {
        const isTree = nearestNode.kind === "wood";
        const newKind = isTree ? "axe" : "pick";
        if (activeToolKind !== newKind) {
          activeToolKind = newKind;
          updateActiveToolMesh();
        }
        const toolLvl = isTree ? state.axeLevel : state.pickaxeLevel;

        if (getBagCount() >= getMaxBag()) {
          if (fullBagWarnCooldown <= 0) {
            spawnFloatingText(playerGroup.position, "BACKPACK FULL!", "#ef4444");
            fullBagWarnCooldown = 1.6;
          }
        } else {
          if (!isMoving) {
            playerGroup.rotation.y = Math.atan2(nearestNode.x - px, nearestNode.z - pz);
          }
          const gatherBase = GATHER_RATES[Math.min(state.gatherSpeedLevel, 8) - 1];
          const toolSpeedBonus = 1 + (toolLvl - 1) * 0.14;
          const drillSpeedBonus = 1 + chainsawLvl * 0.16;
          swingCooldown = 0.52 / (gatherBase * toolSpeedBonus * drillSpeedBonus);
          swingAnimTimer = 0.24;

          const baseDmg = TOOL_DAMAGES[Math.min(toolLvl, 8) - 1];
          const dmg = Math.max(1, Math.round(baseDmg * (1 + chainsawLvl * 0.25)));
          nearestNode.hp -= dmg;
          nearestNode.bounceTimer = 0.22;

          const dmgRatio = 1 - Math.max(0, nearestNode.hp) / nearestNode.maxHp;
          if (nearestNode.crackMesh && dmgRatio > 0.05) {
            nearestNode.crackMesh.visible = true;
            nearestNode.crackMesh.scale.setScalar(0.75 + dmgRatio * 0.5);
          }

          const rDef = RESOURCES[nearestNode.kind];
          playMaterialHitSfx(nearestNode.kind, dmgRatio);
          haptic("light");
          spawnBurst(nearestNode.x, 1.0, nearestNode.z, rDef.color, toolLvl >= 6 ? 7 : 5, 0.14);
          if (chainsawLvl > 0) {
            spawnBurst(nearestNode.x, 0.9, nearestNode.z, 0xf97316, 3, 0.11);
          }

          if (nearestNode.hp <= 0) {
            playMaterialBreakSfx(nearestNode.kind);
            haptic("upgrade");

            if (nearestNode.kind === "wood") {
              nearestNode.fallDirZ = (px <= nearestNode.x) ? -1 : 1;
              nearestNode.fallTimer = nearestNode.fallTotal;
            } else {
              nearestNode.respawnTimer = 4.2;
              nearestNode.mesh.visible = false;
              spawnBurst(nearestNode.x, 0.75, nearestNode.z, rDef.color, 10, 0.22);
              spawnBurst(nearestNode.x, 0.65, nearestNode.z, 0x64748b, 5, 0.2);
            }

            const effLvl = getResUpgradeLevel("gatherEfficiency");
            const effChance = (effLvl - 1) * 0.12 + (state.backpackLevel - 1) * 0.05;
            const effBonus = Math.random() < effChance ? 1 : 0;
            const rareBonus = (!isTree && (nearestNode.kind === "gold" || nearestNode.kind === "crystal" || nearestNode.kind === "rare") && toolLvl >= 4) ? 1 : 0;
            const fleetOreChance = !isTree ? (getFleetLevel("mining_cart") * 0.12 + getFleetLevel("excavator") * 0.22) : 0;
            const fleetBonus = Math.floor(fleetOreChance) + (Math.random() < (fleetOreChance % 1) ? 1 : 0);
            const baseYield = 1 + Math.floor((toolLvl - 1) / 2) + rareBonus + effBonus + fleetBonus;
            const yieldAmt = Math.min(baseYield, getMaxBag() - getBagCount());
            if (yieldAmt > 0) {
              state.inventory[nearestNode.kind] = (state.inventory[nearestNode.kind] || 0) + yieldAmt;
              state.gatheredTotals[nearestNode.kind] = (state.gatheredTotals[nearestNode.kind] || 0) + yieldAmt;
              stateDirty = true;
              refreshCharacterVisuals();
              playSfx("pickup");
              spawnFloatingText(nearestNode.mesh.position, `+${yieldAmt} ${rDef.name}`, rDef.css);
              spawnFlyingDrop(new THREE.Vector3(nearestNode.x, 1.0, nearestNode.z), playerGroup.position, rDef.color);
            }

            const rareLvl = getResUpgradeLevel("rareDiscovery");
            const rareChance = (rareLvl - 1) * 0.08 + (state.pickaxeLevel - 1) * 0.03;
            if (rareChance > 0 && getBagCount() < getMaxBag() && Math.random() < rareChance) {
              const bonusId = state.unlockedBiomes.indexOf("frozen") !== -1 ? "crystal" : "gold";
              const bonusDef = RESOURCES[bonusId];
              state.inventory[bonusId] = (state.inventory[bonusId] || 0) + 1;
              state.gatheredTotals[bonusId] = (state.gatheredTotals[bonusId] || 0) + 1;
              stateDirty = true;
              refreshCharacterVisuals();
              spawnFloatingText(new THREE.Vector3(nearestNode.x, 1.8, nearestNode.z), `+1 Rare ${bonusDef.name}!`, bonusDef.css);
            }
          }
        }
      }

      if (swingAnimTimer > 0) {
        swingAnimTimer -= dt;
        armRPivot.rotation.x = -1.65 * Math.sin((swingAnimTimer / 0.24) * Math.PI);
      } else if (isMenuMode) {
        armRPivot.rotation.x = -0.68 + Math.sin(now * 0.0022) * 0.05;
      } else {
        armRPivot.rotation.x = isMoving ? Math.sin(now * 0.015) * 0.35 : -0.18;
      }

      // 5. Auto-Sell Zone
      sellCooldown = Math.max(0, sellCooldown - dt);
      coinSign.rotation.z += dt * 2.2;
      if (!isMenuMode) {
        const distToSell = Math.hypot(px - SELL_ZONE.x, pz - SELL_ZONE.z);
        if (distToSell <= SELL_ZONE.radius && sellCooldown <= 0) {
          const invKeys = Object.keys(state.inventory).filter((k) => state.inventory[k] > 0);
          if (invKeys.length > 0) {
            const k = invKeys[0];
            const qty = Math.min(state.inventory[k], 3);
            state.inventory[k] -= qty;
            if (state.inventory[k] <= 0) delete state.inventory[k];
            const earned = qty * getSellValue(k);
            state.coins += earned;
            state.lifetimeEarned += earned;
            stateDirty = true;
            sellCooldown = 0.14;
            refreshCharacterVisuals();
            playSfx("coin");
            haptic("coin");
            spawnFloatingText(new THREE.Vector3(SELL_ZONE.x, 1.2, SELL_ZONE.z), `+${formatMoney(earned)}`, "#fde047");
            spawnFlyingDrop(new THREE.Vector3(px, 1.4, pz), new THREE.Vector3(SELL_ZONE.x, 1.1, SELL_ZONE.z), RESOURCES[k].color);
          }
        }
      }

      // 6. Auto-Unlock Biome Gates on Walk-Up Pad
      for (const id in biomeGates) {
        const g = biomeGates[id];
        if (isBiomeUnlocked(id)) continue;
        const dGate = isMenuMode ? 999 : Math.hypot(px - g.biome.gate.x, pz - g.biome.gate.z);
        if (dGate < 2.6) {
          if (!meetsBiomeGear(g.biome)) {
            if (fullBagWarnCooldown <= 0) {
              spawnFloatingText(g.group.position, "Upgrade Gear First!", "#fbbf24");
              fullBagWarnCooldown = 1.6;
            }
            g.charge = 0;
          } else if (state.coins >= g.biome.unlockCost) {
            g.charge += dt;
            const pulse = 1 + Math.sin(now * 0.02) * 0.12;
            g.padRing.scale.set(pulse, pulse, 1);
            if (g.charge >= 0.75) {
              state.coins -= g.biome.unlockCost;
              state.unlockedBiomes.push(id);
              stateDirty = true;
              refreshWorldState();
              playSfx("unlock");
              haptic("unlock");
              spawnBurst(g.biome.gate.x, 1.5, g.biome.gate.z, g.biome.accent, 14, 0.2);
              showBanner(`${g.biome.name} Unlocked!`);
              syncToAndroid(true);
            }
          }
        } else {
          g.charge = 0;
          g.padRing.scale.set(1, 1, 1);
        }
      }

      // 7. World Buildings & Worker Drones
      const autoSpeedBoost = 1 + (getResUpgradeLevel("autoProduction") - 1) * 0.22;
      for (const id in worldBuildings) {
        const wb = worldBuildings[id];
        const lvl = state.buildingLevels[id] || 0;
        const lines = getBuildingLines(id);
        if (lvl > 0) {
          wb.spinner.rotation.z += dt * (1.8 + lvl * 0.35 + lines * 0.5) * autoSpeedBoost;
          wb.workerGroup.rotation.y += dt * 1.4 * autoSpeedBoost;
        }
        if (isMenuMode || !isBiomeUnlocked(wb.def.biome) || lvl >= 8) continue;
        const cost = Math.round(wb.def.baseCost * Math.pow(wb.def.growth, lvl));
        const dBld = Math.hypot(px - wb.def.x, pz - wb.def.z);
        if (dBld < 2.25 && state.coins >= cost) {
          wb.charge += dt;
          if (wb.charge >= 0.85) {
            wb.charge = 0;
            state.coins -= cost;
            state.buildingLevels[id] = lvl + 1;
            stateDirty = true;
            refreshWorldState();
            playSfx("upgrade");
            haptic("upgrade");
            spawnBurst(wb.def.x, 1.5, wb.def.z, 0xfbbf24, 12, 0.2);
            showBanner(`${wb.def.title} Tier ${lvl + 1}!`);
            syncToAndroid(true);
          }
        } else {
          wb.charge = 0;
        }
      }

      // 8. Animate 10 Industrial Machines, Conveyors, Smoke & 5 Fleet Vehicles
      smokeTimer += dt;
      const beltBoost = 1 + getMachineLevel("conveyor_belt") * 0.20;
      for (const id in worldMachines) {
        const wm = worldMachines[id];
        if (!isMachineRunning(id)) continue;
        const mBoost = (1 + (getMachineLevel(id) - 1) * 0.3) * beltBoost;
        for (const rp of wm.anim.rotParts) {
          rp.mesh.rotation[rp.axis] += dt * rp.speed * mBoost;
        }
        for (const p of wm.anim.pistons) {
          p.mesh.position.y = p.baseY + Math.sin(now * 0.001 * p.speed * mBoost) * p.amp;
        }
        for (const c of wm.anim.crates) {
          c.mesh.position[c.axis] += dt * c.speed * mBoost;
          if (c.mesh.position[c.axis] > c.max) c.mesh.position[c.axis] = c.min;
        }
        if (wm.anim.smokeOrigin && smokeTimer > 0.45) {
          const so = wm.anim.smokeOrigin;
          spawnMachineSmoke(wm.def.x + so.lx, so.ly, wm.def.z + so.lz, so.col);
        }
      }
      if (smokeTimer > 0.45) smokeTimer = 0;

      for (let si = machineSmoke.length - 1; si >= 0; si--) {
        const sm = machineSmoke[si];
        sm.age += dt;
        if (sm.age >= sm.life) {
          scene.remove(sm.mesh);
          machineSmoke.splice(si, 1);
          continue;
        }
        sm.mesh.position.y += sm.vy * dt;
        const sc = 1 + sm.age * 1.4;
        sm.mesh.scale.setScalar(sc);
        sm.mesh.material.opacity = Math.max(0, 0.55 * (1 - sm.age / sm.life));
      }

      if (worldFleet.forklift.group.visible) {
        const tFork = now * 0.0011;
        worldFleet.forklift.group.position.z = 1.0 + Math.sin(tFork) * 3.1;
        worldFleet.forklift.group.rotation.y = Math.cos(tFork) >= 0 ? 0 : Math.PI;
        worldFleet.forklift.forkGrp.position.y = 0.45 + Math.sin(now * 0.003) * 0.18;
      }
      if (worldFleet.mining_cart.group.visible) {
        worldFleet.mining_cart.cart.position.x = Math.sin(now * 0.0014) * 2.1;
      }
      if (worldFleet.delivery_truck.group.visible) {
        const pulse = 0.7 + Math.sin(now * 0.008) * 0.6;
        worldFleet.delivery_truck.beacon.material.emissiveIntensity = pulse;
      }
      if (worldFleet.excavator.group.visible) {
        worldFleet.excavator.turret.rotation.y = Math.sin(now * 0.0012) * 0.55;
        worldFleet.excavator.boomPivot.rotation.x = Math.sin(now * 0.0024) * 0.22;
      }

      // 9. Passive Income Tick
      const pps = getPassivePerSec();
      if (pps > 0) {
        passiveAccum += pps * dt;
        if (passiveAccum >= 1) {
          const whole = Math.floor(passiveAccum);
          passiveAccum -= whole;
          state.coins += whole;
          state.lifetimeEarned += whole;
          stateDirty = true;
        }
      }

      // 10. Animate Debris Particles, Flying Drops & Floating Badges
      for (let i = particles.length - 1; i >= 0; i--) {
        const p = particles[i];
        p.age += dt;
        if (p.age >= p.life) { scene.remove(p.mesh); particles.splice(i, 1); continue; }
        p.vy -= 15 * dt;
        p.mesh.position.x += p.vx * dt;
        p.mesh.position.y = Math.max(0.1, p.mesh.position.y + p.vy * dt);
        p.mesh.position.z += p.vz * dt;
        p.mesh.rotation.x += p.rotSpd * dt;
        p.mesh.rotation.y += p.rotSpd * dt;
        p.mesh.scale.setScalar(p.baseScale * (1 - p.age / p.life));
      }

      for (let i = flyingDrops.length - 1; i >= 0; i--) {
        const f = flyingDrops[i];
        f.t += dt;
        const u = Math.min(1, f.t / f.dur);
        f.mesh.position.x = f.start.x + (f.target.x - f.start.x) * u;
        f.mesh.position.z = f.start.z + (f.target.z - f.start.z) * u;
        f.mesh.position.y = f.start.y + ((f.target.y || 1.1) - f.start.y) * u + Math.sin(u * Math.PI) * 1.4;
        f.mesh.rotation.y += dt * 10;
        if (u >= 1) { scene.remove(f.mesh); flyingDrops.splice(i, 1); }
      }

      const { w: vpW, h: vpH } = getViewportSize();
      const hw = vpW / 2, hh = vpH / 2;
      const proj = new THREE.Vector3();
      for (let i = floatingBadges.length - 1; i >= 0; i--) {
        const b = floatingBadges[i];
        b.age += dt;
        if (b.age >= b.life) { b.el.remove(); floatingBadges.splice(i, 1); continue; }
        proj.set(b.wx, b.wy + b.age * 1.6, b.wz);
        proj.project(camera);
        b.el.style.left = ((proj.x * hw) + hw) + "px";
        b.el.style.top = (-(proj.y * hh) + hh) + "px";
        b.el.style.opacity = String(1 - Math.pow(b.age / b.life, 2));
      }

      syncToAndroid(false);
      renderer.render(scene, camera);
    } catch (err) {
      console.error("Tick error:", err);
    }
  }

  refreshCharacterVisuals();
  refreshWorldState();
  syncViewportSize();
  window.setMenuMode(true);
  renderer.render(scene, camera);
  document.getElementById("loading-overlay").classList.add("hidden");
  if (window.AndroidBridge && window.AndroidBridge.onEngineReady) {
    try { window.AndroidBridge.onEngineReady(); } catch (_) {}
  }
  requestAnimationFrame(tick);
})();
