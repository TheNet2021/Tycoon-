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
    { id: "forest",    name: "Green Forest",    cx: 0,   cz: 0,   unlockCost: 0,     sky: 0x67b8e3, ground: 0x3b8c47, accent: 0x10b981, gate: null },
    { id: "mountains", name: "Rocky Mountains", cx: 0,   cz: -34, unlockCost: 150,   sky: 0x7c93a8, ground: 0x5c6672, accent: 0x94a3b8, gate: { x: 0, z: -17, dir: "z" } },
    { id: "desert",    name: "Golden Desert",   cx: 34,  cz: 0,   unlockCost: 650,   sky: 0xf3b86c, ground: 0xd9a14a, accent: 0xf59e0b, gate: { x: 17, z: 0, dir: "x" } },
    { id: "frozen",    name: "Frozen Lands",    cx: -34, cz: 0,   unlockCost: 2000,  sky: 0x92c8e8, ground: 0xcbe7f5, accent: 0x38bdf8, gate: { x: -17, z: 0, dir: "x" } },
    { id: "volcanic",  name: "Volcanic Region", cx: 0,   cz: 34,  unlockCost: 6500,  sky: 0x451a1a, ground: 0x292524, accent: 0xef4444, gate: { x: 0, z: 17, dir: "z" } },
    { id: "jungle",    name: "Ancient Jungle",  cx: 34,  cz: -34, unlockCost: 18000, sky: 0x3b968d, ground: 0x1e5c4a, accent: 0x14b8a6, gate: { x: 34, z: -17, dir: "z" } }
  ];

  const BUILDING_DEFS = [
    { id: "sawmill",        title: "Timber Sawmill",   biome: "forest",    x: 7.5,   z: 6.5,   baseCost: 120,   growth: 2.3, passive: 4 },
    { id: "warehouse",      title: "Warehouse",        biome: "forest",    x: 7.5,   z: -4.5,  baseCost: 180,   growth: 2.4, passive: 2 },
    { id: "miner_forest",   title: "Lumber Bot",       biome: "forest",    x: -8.0,  z: -6.0,  baseCost: 90,    growth: 2.2, passive: 3 },
    { id: "smelter",        title: "Ore Smelter",      biome: "mountains", x: 7.5,   z: -29.0, baseCost: 320,   growth: 2.4, passive: 10 },
    { id: "miner_mountains",title: "Drill Rig",        biome: "mountains", x: -7.5,  z: -29.0, baseCost: 350,   growth: 2.2, passive: 12 },
    { id: "miner_desert",   title: "Solar Extractor",  biome: "desert",    x: 34.0,  z: 7.5,   baseCost: 950,   growth: 2.2, passive: 30 },
    { id: "miner_frozen",   title: "Cryo Harvester",   biome: "frozen",    x: -34.0, z: 7.5,   baseCost: 2800,  growth: 2.2, passive: 75 },
    { id: "miner_volcanic", title: "Magma Extractor",  biome: "volcanic",  x: 7.5,   z: 34.0,  baseCost: 8000,  growth: 2.2, passive: 190 },
    { id: "miner_jungle",   title: "Relic Synth",      biome: "jungle",    x: 34.0,  z: -26.5, baseCost: 22000, growth: 2.2, passive: 450 }
  ];

  const BAG_CAPS = [12, 22, 36, 56, 85, 130];
  const MOVE_SPEEDS = [6.4, 7.6, 8.9, 10.4, 12.0, 14.0];
  const GATHER_RATES = [1.0, 1.3, 1.65, 2.05, 2.55, 3.2];

  const TIER_COLORS = [
    0x92400e, // 1: Primitive wood/stone
    0x94a3b8, // 2: Forged iron
    0xea580c, // 3: Copper-steel
    0xfbbf24, // 4: Gilded gold
    0x22d3ee, // 5: Prism crystal
    0xe879f9  // 6: Mythic reactor
  ];

  // =========================================================================
  // 2. GAME STATE
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
    buildingLevels: {},
    unlockedBiomes: ["forest"],
    claimedQuestIds: [],
    uniqueItems: [],
    isMuted: false,
    hapticsEnabled: true
  };

  let stateDirty = false;
  let lastSyncTime = 0;

  function hasUniqueItem(id) {
    return Array.isArray(state.uniqueItems) && state.uniqueItems.indexOf(id) !== -1;
  }

  function getMaxBag() {
    const base = BAG_CAPS[Math.min(Math.max(state.backpackLevel, 1), 6) - 1];
    const wh = state.buildingLevels.warehouse || 0;
    const lantern = hasUniqueItem("prism_lantern") ? 10 : 0;
    return base + wh * 8 + lantern;
  }

  function getBagCount() {
    let sum = 0;
    for (const k in state.inventory) sum += state.inventory[k] || 0;
    return sum;
  }

  function getPassivePerSec() {
    let total = 0;
    for (const b of BUILDING_DEFS) {
      total += (state.buildingLevels[b.id] || 0) * b.passive;
    }
    if (hasUniqueItem("tycoon_crown")) total *= 1.35;
    return total;
  }

  function getSellValue(resourceId) {
    const def = RESOURCES[resourceId];
    if (!def) return 5;
    let mult = 1.0 + (state.buildingLevels.warehouse || 0) * 0.2;
    if (resourceId === "wood") {
      mult += (state.buildingLevels.sawmill || 0) * 0.45;
      if (hasUniqueItem("timber_charm")) mult += 0.25;
    }
    if (resourceId === "stone" || resourceId === "coal" || resourceId === "copper" || resourceId === "iron" || resourceId === "gold") {
      mult += (state.buildingLevels.smelter || 0) * 0.45;
      if (hasUniqueItem("pickaxe_crest")) mult += 0.25;
    }
    if (resourceId === "crystal" && hasUniqueItem("prism_lantern")) {
      mult += 0.25;
    }
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
  // 3. PROCEDURAL WEB AUDIO SYNTHESIZER
  // =========================================================================
  let audioCtx = null;
  function ensureAudio() {
    if (state.isMuted) return null;
    if (!audioCtx) {
      const AC = window.AudioContext || window.webkitAudioContext;
      if (AC) audioCtx = new AC();
    }
    if (audioCtx && audioCtx.state === "suspended") {
      audioCtx.resume().catch(() => {});
    }
    return audioCtx;
  }

  function playTone(freq, endFreq, dur, type, vol) {
    if (state.isMuted) return;
    const ctx = ensureAudio();
    if (!ctx) return;
    const now = ctx.currentTime;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = type || "triangle";
    osc.frequency.setValueAtTime(freq, now);
    if (endFreq && endFreq !== freq) {
      osc.frequency.exponentialRampToValueAtTime(Math.max(20, endFreq), now + dur);
    }
    gain.gain.setValueAtTime(vol || 0.12, now);
    gain.gain.exponentialRampToValueAtTime(0.001, now + dur);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start(now);
    osc.stop(now + dur + 0.01);
  }

  function playSfx(kind) {
    if (state.isMuted) return;
    if (kind === "chop") {
      playTone(170, 75, 0.09, "triangle", 0.16);
    } else if (kind === "mine") {
      playTone(620, 280, 0.08, "square", 0.07);
      playTone(980, 740, 0.06, "sine", 0.08);
    } else if (kind === "crystal") {
      playTone(980, 1480, 0.14, "sine", 0.1);
    } else if (kind === "pickup") {
      playTone(520, 780, 0.07, "sine", 0.09);
    } else if (kind === "coin") {
      playTone(987, 1318, 0.08, "sine", 0.11);
      setTimeout(() => playTone(1318, 1567, 0.1, "sine", 0.1), 45);
    } else if (kind === "unlock") {
      [523, 659, 784, 1046].forEach((f, i) => {
        setTimeout(() => playTone(f, f * 1.02, 0.18, "triangle", 0.13), i * 75);
      });
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
  const biomePillEl = document.getElementById("biome-pill");
  let bannerTimer = null;

  function showBanner(msg) {
    bannerEl.textContent = msg;
    bannerEl.classList.add("visible");
    clearTimeout(bannerTimer);
    bannerTimer = setTimeout(() => bannerEl.classList.remove("visible"), 2200);
  }

  function spawnFloatingText(worldPos, text, colorCss) {
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
  // 5. THREE.JS SCENE SETUP
  // =========================================================================
  const canvas = document.getElementById("game-canvas");
  const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, powerPreference: "high-performance" });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  renderer.outputEncoding = THREE.sRGBEncoding;

  const scene = new THREE.Scene();
  const currentSkyColor = new THREE.Color(0x67b8e3);
  scene.background = currentSkyColor;
  scene.fog = new THREE.Fog(currentSkyColor.clone(), 28, 72);

  const camera = new THREE.PerspectiveCamera(50, window.innerWidth / window.innerHeight, 0.5, 140);
  camera.position.set(0, 14.5, 13.5);

  window.addEventListener("resize", () => {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
  });

  // Lighting
  const hemiLight = new THREE.HemisphereLight(0xfffbeb, 0x1e293b, 0.72);
  scene.add(hemiLight);

  const sunLight = new THREE.DirectionalLight(0xfff3d1, 1.05);
  sunLight.position.set(14, 24, 12);
  sunLight.castShadow = true;
  sunLight.shadow.mapSize.width = 1024;
  sunLight.shadow.mapSize.height = 1024;
  sunLight.shadow.camera.near = 2;
  sunLight.shadow.camera.far = 75;
  const d = 22;
  sunLight.shadow.camera.left = -d;
  sunLight.shadow.camera.right = d;
  sunLight.shadow.camera.top = d;
  sunLight.shadow.camera.bottom = -d;
  sunLight.shadow.bias = -0.0008;
  scene.add(sunLight);
  scene.add(sunLight.target);

  // Reusable shared geometries
  const boxGeo = new THREE.BoxGeometry(1, 1, 1);
  const coneGeo = new THREE.ConeGeometry(1, 1, 6);
  const cylGeo = new THREE.CylinderGeometry(1, 1, 1, 7);
  const dodecaGeo = new THREE.DodecahedronGeometry(1, 0);
  const octaGeo = new THREE.OctahedronGeometry(1, 0);

  function flatMat(color, opts) {
    return new THREE.MeshStandardMaterial(Object.assign({
      color,
      roughness: 0.78,
      metalness: 0.08,
      flatShading: true
    }, opts || {}));
  }

  // =========================================================================
  // 6. WORLD TERRAIN, PATHS, GATES, TRADING ZONE, BUILDINGS & NODES
  // =========================================================================
  const worldGroup = new THREE.Group();
  scene.add(worldGroup);

  // Base ocean/abyss floor beneath platforms
  const abyssMesh = new THREE.Mesh(
    new THREE.PlaneGeometry(240, 240),
    new THREE.MeshBasicMaterial({ color: 0x0f172a })
  );
  abyssMesh.rotation.x = -Math.PI / 2;
  abyssMesh.position.y = -0.6;
  worldGroup.add(abyssMesh);

  // Build biome platforms & connecting bridges
  BIOMES.forEach((b) => {
    const pad = new THREE.Mesh(
      new THREE.BoxGeometry(31.5, 1.0, 31.5),
      flatMat(b.ground, { roughness: 0.88 })
    );
    pad.position.set(b.cx, -0.5, b.cz);
    pad.receiveShadow = true;
    worldGroup.add(pad);

    // Decorative border bevel
    const rim = new THREE.Mesh(
      new THREE.BoxGeometry(32.2, 0.45, 32.2),
      flatMat(b.accent, { roughness: 0.7 })
    );
    rim.position.set(b.cx, -0.82, b.cz);
    worldGroup.add(rim);
  });

  // Connecting bridges
  function addBridge(x, z, rotY) {
    const br = new THREE.Mesh(
      new THREE.BoxGeometry(5.6, 0.6, 6.0),
      flatMat(0x78350f)
    );
    br.position.set(x, -0.32, z);
    br.rotation.y = rotY;
    br.receiveShadow = true;
    worldGroup.add(br);
  }
  addBridge(0, -17, 0);    // Forest <-> Mountains
  addBridge(17, 0, Math.PI / 2); // Forest <-> Desert
  addBridge(-17, 0, Math.PI / 2);// Forest <-> Frozen
  addBridge(0, 17, 0);     // Forest <-> Volcanic
  addBridge(34, -17, 0);   // Desert <-> Jungle

  // Central Forest cobble plaza
  const plaza = new THREE.Mesh(
    new THREE.CylinderGeometry(8.5, 8.8, 0.14, 10),
    flatMat(0xd6d3d1)
  );
  plaza.position.set(0, 0.02, 0);
  plaza.receiveShadow = true;
  worldGroup.add(plaza);

  // Trading Post (Sell Zone) at (-5.2, 6.2)
  const SELL_ZONE = { x: -5.2, z: 6.2, radius: 3.1 };
  const sellPadGroup = new THREE.Group();
  sellPadGroup.position.set(SELL_ZONE.x, 0.05, SELL_ZONE.z);
  worldGroup.add(sellPadGroup);

  const sellRing = new THREE.Mesh(
    new THREE.RingGeometry(2.3, 2.85, 24),
    new THREE.MeshBasicMaterial({ color: 0xfbbf24, side: THREE.DoubleSide })
  );
  sellRing.rotation.x = -Math.PI / 2;
  sellRing.position.y = 0.04;
  sellPadGroup.add(sellRing);

  const sellFloor = new THREE.Mesh(
    new THREE.CylinderGeometry(2.3, 2.3, 0.1, 16),
    flatMat(0xf59e0b, { emissive: 0x78350f, emissiveIntensity: 0.35 })
  );
  sellFloor.receiveShadow = true;
  sellPadGroup.add(sellFloor);

  // Market Stall Canopy behind sell pad
  const stall = new THREE.Group();
  stall.position.set(-1.9, 0, 1.9);
  for (const [px, pz] of [[-0.9, -0.7], [0.9, -0.7], [-0.9, 0.7], [0.9, 0.7]]) {
    const post = new THREE.Mesh(boxGeo, flatMat(0x78350f));
    post.scale.set(0.18, 2.0, 0.18);
    post.position.set(px, 1.0, pz);
    post.castShadow = true;
    stall.add(post);
  }
  const roof = new THREE.Mesh(boxGeo, flatMat(0xef4444));
  roof.scale.set(2.4, 0.25, 2.0);
  roof.position.set(0, 2.05, 0);
  roof.castShadow = true;
  stall.add(roof);
  const coinSign = new THREE.Mesh(
    new THREE.CylinderGeometry(0.55, 0.55, 0.14, 12),
    flatMat(0xfbbf24, { emissive: 0xd97706, emissiveIntensity: 0.45, metalness: 0.4 })
  );
  coinSign.rotation.x = Math.PI / 2;
  coinSign.position.set(0, 2.95, 0);
  stall.add(coinSign);
  sellPadGroup.add(stall);

  // Biome Gates
  const biomeGates = {};
  BIOMES.forEach((b) => {
    if (!b.gate) return;
    const gGroup = new THREE.Group();
    gGroup.position.set(b.gate.x, 0, b.gate.z);
    if (b.gate.dir === "x") gGroup.rotation.y = Math.PI / 2;

    const pillarL = new THREE.Mesh(boxGeo, flatMat(0x475569));
    pillarL.scale.set(0.8, 3.2, 0.8);
    pillarL.position.set(-2.4, 1.6, 0);
    pillarL.castShadow = true;
    gGroup.add(pillarL);

    const pillarR = pillarL.clone();
    pillarR.position.set(2.4, 1.6, 0);
    gGroup.add(pillarR);

    const barrier = new THREE.Mesh(
      boxGeo,
      new THREE.MeshStandardMaterial({
        color: b.accent,
        transparent: true,
        opacity: 0.55,
        emissive: b.accent,
        emissiveIntensity: 0.4
      })
    );
    barrier.scale.set(4.1, 2.5, 0.35);
    barrier.position.set(0, 1.25, 0);
    gGroup.add(barrier);

    const padRing = new THREE.Mesh(
      new THREE.RingGeometry(1.4, 1.85, 20),
      new THREE.MeshBasicMaterial({ color: 0xfbbf24, side: THREE.DoubleSide })
    );
    padRing.rotation.x = -Math.PI / 2;
    padRing.position.set(0, 0.08, 0);
    gGroup.add(padRing);

    worldGroup.add(gGroup);
    biomeGates[b.id] = {
      biome: b,
      group: gGroup,
      barrier,
      padRing,
      charge: 0
    };
  });

  // World Buildings (Pads + 3D animated structures when built)
  const worldBuildings = {};
  BUILDING_DEFS.forEach((def) => {
    const grp = new THREE.Group();
    grp.position.set(def.x, 0, def.z);

    const padMesh = new THREE.Mesh(
      new THREE.CylinderGeometry(1.9, 1.9, 0.12, 12),
      flatMat(0x334155)
    );
    padMesh.position.y = 0.05;
    padMesh.receiveShadow = true;
    grp.add(padMesh);

    const ring = new THREE.Mesh(
      new THREE.RingGeometry(1.65, 1.95, 20),
      new THREE.MeshBasicMaterial({ color: 0x10b981, side: THREE.DoubleSide })
    );
    ring.rotation.x = -Math.PI / 2;
    ring.position.y = 0.13;
    grp.add(ring);

    // Constructed mesh container (hidden until level >= 1)
    const struct = new THREE.Group();
    struct.visible = false;

    const base = new THREE.Mesh(boxGeo, flatMat(def.id === "smelter" ? 0x475569 : 0x92400e));
    base.scale.set(2.1, 1.4, 2.1);
    base.position.y = 0.75;
    base.castShadow = true;
    struct.add(base);

    const spinner = new THREE.Mesh(
      def.id.startsWith("miner") ? coneGeo : cylGeo,
      flatMat(def.id === "smelter" ? 0xf97316 : 0x38bdf8, {
        emissive: def.id === "smelter" ? 0xea580c : 0x0284c7,
        emissiveIntensity: 0.35
      })
    );
    spinner.scale.set(0.7, 0.9, 0.7);
    spinner.position.y = 1.95;
    spinner.castShadow = true;
    struct.add(spinner);

    grp.add(struct);
    worldGroup.add(grp);
    worldBuildings[def.id] = {
      def,
      group: grp,
      padMesh,
      ring,
      struct,
      spinner,
      charge: 0
    };
  });

  // =========================================================================
  // 7. RESOURCE NODES & DROPS
  // =========================================================================
  const resourceNodes = [];
  const flyingDrops = [];
  const particles = [];

  function createNodeMesh(kind, biomeId) {
    const grp = new THREE.Group();
    if (kind === "wood") {
      const trunk = new THREE.Mesh(cylGeo, flatMat(0x78350f));
      trunk.scale.set(0.32, 1.2, 0.32);
      trunk.position.y = 0.6;
      trunk.castShadow = true;
      grp.add(trunk);

      const leafColor = biomeId === "frozen" ? 0x99f6e4 : (biomeId === "jungle" ? 0x059669 : 0x16a34a);
      const c1 = new THREE.Mesh(coneGeo, flatMat(leafColor));
      c1.scale.set(1.15, 1.5, 1.15);
      c1.position.y = 1.65;
      c1.castShadow = true;
      grp.add(c1);

      const c2 = new THREE.Mesh(coneGeo, flatMat(leafColor));
      c2.scale.set(0.85, 1.25, 0.85);
      c2.position.y = 2.45;
      c2.castShadow = true;
      grp.add(c2);
    } else if (kind === "crystal" || kind === "rare") {
      const col = RESOURCES[kind].color;
      for (let i = 0; i < 3; i++) {
        const shard = new THREE.Mesh(octaGeo, flatMat(col, {
          emissive: col,
          emissiveIntensity: 0.45,
          roughness: 0.25,
          metalness: 0.3
        }));
        shard.scale.set(0.45 - i * 0.08, 1.05 - i * 0.15, 0.45 - i * 0.08);
        shard.position.set((i - 1) * 0.32, 0.65, (i % 2 === 0 ? 0.15 : -0.15));
        shard.rotation.z = (i - 1) * 0.28;
        shard.castShadow = true;
        grp.add(shard);
      }
    } else {
      // Mineral boulders & ore veins
      const baseRock = new THREE.Mesh(dodecaGeo, flatMat(0x64748b));
      baseRock.scale.set(0.95, 0.78, 0.9);
      baseRock.position.y = 0.55;
      baseRock.castShadow = true;
      grp.add(baseRock);

      if (kind !== "stone") {
        const oreCol = RESOURCES[kind].color;
        for (let i = 0; i < 3; i++) {
          const nugget = new THREE.Mesh(boxGeo, flatMat(oreCol, {
            emissive: kind === "gold" ? 0xb45309 : 0x000000,
            emissiveIntensity: 0.3,
            metalness: 0.5
          }));
          nugget.scale.set(0.32, 0.32, 0.32);
          const ang = i * 2.1;
          nugget.position.set(Math.cos(ang) * 0.52, 0.65 + (i % 2) * 0.2, Math.sin(ang) * 0.52);
          nugget.rotation.set(0.4, ang, 0.3);
          grp.add(nugget);
        }
      }
    }
    return grp;
  }

  function populateBiomeNodes() {
    const specs = [
      // Green Forest
      { biome: "forest", kind: "wood", x: -10, z: 9, hp: 2 },
      { biome: "forest", kind: "wood", x: -12, z: 3, hp: 2 },
      { biome: "forest", kind: "wood", x: -11, z: -11, hp: 2 },
      { biome: "forest", kind: "wood", x: 10, z: -10, hp: 2 },
      { biome: "forest", kind: "wood", x: 12, z: 11, hp: 2 },
      { biome: "forest", kind: "wood", x: 2, z: 12, hp: 2 },
      { biome: "forest", kind: "stone", x: -4, z: -11, hp: 3 },
      { biome: "forest", kind: "stone", x: 11, z: 1, hp: 3 },
      { biome: "forest", kind: "stone", x: -12, z: -2, hp: 3 },

      // Rocky Mountains
      { biome: "mountains", kind: "stone", x: -10, z: -40, hp: 3 },
      { biome: "mountains", kind: "coal",  x: -4,  z: -43, hp: 4 },
      { biome: "mountains", kind: "coal",  x: 11,  z: -36, hp: 4 },
      { biome: "mountains", kind: "iron",  x: 4,   z: -44, hp: 5 },
      { biome: "mountains", kind: "iron",  x: -11, z: -34, hp: 5 },
      { biome: "mountains", kind: "iron",  x: 10,  z: -24, hp: 5 },
      { biome: "mountains", kind: "wood",  x: -6,  z: -22, hp: 3 },

      // Golden Desert
      { biome: "desert", kind: "copper", x: 26, z: -9,  hp: 5 },
      { biome: "desert", kind: "copper", x: 42, z: -6,  hp: 5 },
      { biome: "desert", kind: "copper", x: 25, z: 9,   hp: 5 },
      { biome: "desert", kind: "gold",   x: 43, z: 8,   hp: 7 },
      { biome: "desert", kind: "gold",   x: 38, z: -11, hp: 7 },
      { biome: "desert", kind: "stone",  x: 30, z: 12,  hp: 4 },

      // Frozen Lands
      { biome: "frozen", kind: "crystal", x: -42, z: -8, hp: 8 },
      { biome: "frozen", kind: "crystal", x: -26, z: -10, hp: 8 },
      { biome: "frozen", kind: "crystal", x: -43, z: 9,  hp: 8 },
      { biome: "frozen", kind: "iron",    x: -25, z: 10, hp: 6 },
      { biome: "frozen", kind: "wood",    x: -36, z: -11, hp: 4 },
      { biome: "frozen", kind: "wood",    x: -44, z: 0,  hp: 4 },

      // Volcanic Region
      { biome: "volcanic", kind: "coal", x: -10, z: 26, hp: 6 },
      { biome: "volcanic", kind: "gold", x: 11,  z: 25, hp: 8 },
      { biome: "volcanic", kind: "gold", x: -11, z: 42, hp: 8 },
      { biome: "volcanic", kind: "rare", x: 0,   z: 44, hp: 10 },
      { biome: "volcanic", kind: "rare", x: 11,  z: 43, hp: 10 },

      // Ancient Jungle
      { biome: "jungle", kind: "wood",    x: 24, z: -42, hp: 5 },
      { biome: "jungle", kind: "wood",    x: 44, z: -26, hp: 5 },
      { biome: "jungle", kind: "crystal", x: 24, z: -24, hp: 9 },
      { biome: "jungle", kind: "rare",    x: 43, z: -42, hp: 12 },
      { biome: "jungle", kind: "rare",    x: 34, z: -44, hp: 12 }
    ];

    specs.forEach((sp, i) => {
      const mesh = createNodeMesh(sp.kind, sp.biome);
      mesh.position.set(sp.x, 0, sp.z);
      mesh.rotation.y = (i * 1.37) % (Math.PI * 2);
      worldGroup.add(mesh);
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
        mesh
      });
    });
  }
  populateBiomeNodes();

  // =========================================================================
  // 8. EVOLVING 3D PLAYER CHARACTER
  // =========================================================================
  const playerGroup = new THREE.Group();
  playerGroup.position.set(0, 0, 2);
  scene.add(playerGroup);

  const playerBodyPivot = new THREE.Group();
  playerGroup.add(playerBodyPivot);

  const torsoMat = flatMat(0x2563eb);
  const torso = new THREE.Mesh(boxGeo, torsoMat);
  torso.scale.set(0.72, 0.85, 0.45);
  torso.position.y = 1.05;
  torso.castShadow = true;
  playerBodyPivot.add(torso);

  const head = new THREE.Mesh(boxGeo, flatMat(0xfed7aa));
  head.scale.set(0.56, 0.56, 0.56);
  head.position.y = 1.8;
  head.castShadow = true;
  playerBodyPivot.add(head);

  // Eyes
  const eyeMat = flatMat(0x0f172a);
  const eyeL = new THREE.Mesh(boxGeo, eyeMat);
  eyeL.scale.set(0.08, 0.1, 0.06);
  eyeL.position.set(-0.13, 1.83, 0.28);
  playerBodyPivot.add(eyeL);
  const eyeR = eyeL.clone();
  eyeR.position.x = 0.13;
  playerBodyPivot.add(eyeR);

  // Evolving Hat / Helmet
  const hatMat = flatMat(0x10b981);
  const hat = new THREE.Mesh(boxGeo, hatMat);
  hat.scale.set(0.64, 0.2, 0.64);
  hat.position.y = 2.13;
  hat.castShadow = true;
  playerBodyPivot.add(hat);

  const helmetCrest = new THREE.Mesh(octaGeo, flatMat(0x22d3ee, { emissive: 0x06b6d4, emissiveIntensity: 0.5 }));
  helmetCrest.scale.set(0.18, 0.22, 0.18);
  helmetCrest.position.set(0, 2.28, 0.2);
  helmetCrest.visible = false;
  playerBodyPivot.add(helmetCrest);

  // Legs
  const legMat = flatMat(0x334155);
  const legL = new THREE.Mesh(boxGeo, legMat);
  legL.scale.set(0.26, 0.62, 0.28);
  legL.position.set(-0.18, 0.31, 0);
  legL.castShadow = true;
  playerBodyPivot.add(legL);
  const legR = legL.clone();
  legR.position.x = 0.18;
  playerBodyPivot.add(legR);

  // Left Arm
  const armL = new THREE.Mesh(boxGeo, flatMat(0xfed7aa));
  armL.scale.set(0.22, 0.68, 0.22);
  armL.position.set(-0.5, 1.05, 0);
  armL.castShadow = true;
  playerBodyPivot.add(armL);

  // Right Arm Pivot + Evolving Tool (Axe / Pickaxe)
  const armRPivot = new THREE.Group();
  armRPivot.position.set(0.5, 1.35, 0);
  playerBodyPivot.add(armRPivot);

  const armR = new THREE.Mesh(boxGeo, flatMat(0xfed7aa));
  armR.scale.set(0.22, 0.65, 0.22);
  armR.position.set(0, -0.28, 0);
  armR.castShadow = true;
  armRPivot.add(armR);

  const toolGroup = new THREE.Group();
  toolGroup.position.set(0, -0.55, 0.28);
  armRPivot.add(toolGroup);

  const toolHandle = new THREE.Mesh(boxGeo, flatMat(0x78350f));
  toolHandle.scale.set(0.09, 0.09, 0.92);
  toolHandle.position.z = 0.25;
  toolGroup.add(toolHandle);

  const toolHeadMat = flatMat(TIER_COLORS[0], { metalness: 0.35, roughness: 0.4 });
  const axeHead = new THREE.Mesh(boxGeo, toolHeadMat);
  axeHead.scale.set(0.12, 0.34, 0.26);
  axeHead.position.set(0, 0.14, 0.62);
  toolGroup.add(axeHead);

  const pickHead = new THREE.Mesh(boxGeo, toolHeadMat);
  pickHead.scale.set(0.58, 0.1, 0.14);
  pickHead.position.set(0, 0.04, 0.65);
  pickHead.visible = false;
  toolGroup.add(pickHead);

  // Evolving Backpack + Live Stacked Resource Cubes
  const backpackMat = flatMat(0x92400e);
  const backpackMesh = new THREE.Mesh(boxGeo, backpackMat);
  backpackMesh.scale.set(0.56, 0.68, 0.32);
  backpackMesh.position.set(0, 1.08, -0.38);
  backpackMesh.castShadow = true;
  playerBodyPivot.add(backpackMesh);

  const bagStackGroup = new THREE.Group();
  bagStackGroup.position.set(0, 1.48, -0.4);
  playerBodyPivot.add(bagStackGroup);

  function refreshCharacterVisuals() {
    const axeCol = TIER_COLORS[Math.min(state.axeLevel, 6) - 1];
    const pickCol = TIER_COLORS[Math.min(state.pickaxeLevel, 6) - 1];
    const bagLvl = Math.min(state.backpackLevel, 6);
    const bagCol = TIER_COLORS[bagLvl - 1];

    backpackMat.color.setHex(bagCol);
    const s = 1 + (bagLvl - 1) * 0.13;
    backpackMesh.scale.set(0.54 * s, 0.65 * s, 0.3 * s);
    backpackMesh.position.z = -0.35 - (bagLvl - 1) * 0.03;

    // Outfit evolution based on average gear level
    const avgGear = (state.axeLevel + state.pickaxeLevel + state.backpackLevel) / 3;
    if (avgGear >= 4.5) {
      torsoMat.color.setHex(0x06b6d4);
      hatMat.color.setHex(0xfbbf24);
      helmetCrest.visible = true;
    } else if (avgGear >= 2.5) {
      torsoMat.color.setHex(0xf59e0b);
      hatMat.color.setHex(0x94a3b8);
      helmetCrest.visible = false;
    } else {
      torsoMat.color.setHex(0x2563eb);
      hatMat.color.setHex(0x10b981);
      helmetCrest.visible = false;
    }

    // Active tool color preview
    toolHeadMat.color.setHex(pickHead.visible ? pickCol : axeCol);
    if ((pickHead.visible ? state.pickaxeLevel : state.axeLevel) >= 5) {
      toolHeadMat.emissive.setHex(pickHead.visible ? pickCol : axeCol);
      toolHeadMat.emissiveIntensity = 0.45;
    } else {
      toolHeadMat.emissiveIntensity = 0;
    }

    // Rebuild stacked resource blocks on backpack
    while (bagStackGroup.children.length > 0) {
      bagStackGroup.remove(bagStackGroup.children[0]);
    }
    const keys = Object.keys(state.inventory).filter((k) => state.inventory[k] > 0);
    let idx = 0;
    for (const k of keys) {
      const count = Math.min(state.inventory[k], 4);
      const col = RESOURCES[k] ? RESOURCES[k].color : 0xffffff;
      for (let c = 0; c < count && idx < 10; c++) {
        const cube = new THREE.Mesh(boxGeo, flatMat(col));
        cube.scale.set(0.24, 0.14, 0.24);
        cube.position.set((idx % 2 === 0 ? -0.13 : 0.13), Math.floor(idx / 2) * 0.15, 0);
        bagStackGroup.add(cube);
        idx++;
      }
    }
  }

  // =========================================================================
  // 9. PARTICLES & FLYING RESOURCE ARCS
  // =========================================================================
  function spawnBurst(x, y, z, colorHex, count) {
    for (let i = 0; i < (count || 6); i++) {
      const m = new THREE.Mesh(boxGeo, flatMat(colorHex));
      m.scale.set(0.16, 0.16, 0.16);
      m.position.set(x, y, z);
      scene.add(m);
      const ang = Math.random() * Math.PI * 2;
      const spd = 2.0 + Math.random() * 3.5;
      particles.push({
        mesh: m,
        vx: Math.cos(ang) * spd,
        vy: 3.0 + Math.random() * 3.2,
        vz: Math.sin(ang) * spd,
        age: 0,
        life: 0.55
      });
    }
  }

  function spawnFlyingDrop(fromPos, toTarget, colorHex, onArrive) {
    const m = new THREE.Mesh(boxGeo, flatMat(colorHex, { emissive: colorHex, emissiveIntensity: 0.25 }));
    m.scale.set(0.28, 0.28, 0.28);
    m.position.copy(fromPos);
    scene.add(m);
    flyingDrops.push({
      mesh: m,
      start: fromPos.clone(),
      target: toTarget,
      t: 0,
      dur: 0.32,
      onArrive
    });
  }

  // =========================================================================
  // 10. VIRTUAL JOYSTICK (ONE-HAND CONTROL ONLY)
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
    if (activePointerId !== null) return;
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
    if (e.pointerId !== activePointerId) return;
    activePointerId = null;
    moveX = 0;
    moveZ = 0;
    joyKnob.style.transform = "translate3d(0px, 0px, 0)";
    joyBase.classList.remove("active");
  }
  joyZone.addEventListener("pointerup", resetJoystick);
  joyZone.addEventListener("pointercancel", resetJoystick);

  // =========================================================================
  // 11. MOVEMENT COLLISION & BIOME BOUNDS
  // =========================================================================
  function isWalkable(x, z) {
    // Check if inside any unlocked biome pad or connecting bridge
    for (const b of BIOMES) {
      if (!isBiomeUnlocked(b.id)) continue;
      if (Math.abs(x - b.cx) <= 15.0 && Math.abs(z - b.cz) <= 15.0) return true;
    }
    // Bridges between unlocked adjacent biomes
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
      const d = Math.hypot(x - b.cx, z - b.cz);
      if (d < bestDist) {
        bestDist = d;
        best = b;
      }
    }
    return best;
  }

  // =========================================================================
  // 12. ANDROID BRIDGE & HYDRATION
  // =========================================================================
  function syncToAndroid(force) {
    const now = performance.now();
    if (!force && (!stateDirty || now - lastSyncTime < 250)) return;
    stateDirty = false;
    lastSyncTime = now;
    if (window.AndroidBridge && window.AndroidBridge.syncState) {
      try {
        window.AndroidBridge.syncState(JSON.stringify(state));
      } catch (_) {}
    }
  }

  window.applyGameState = function (incoming) {
    if (!incoming) return;
    state = Object.assign({}, state, incoming);
    refreshCharacterVisuals();
    refreshWorldState();
  };

  window.teleportPlayer = function (x, z, biomeId) {
    playerGroup.position.set(x, 0, z);
    showBanner("Traveled to " + (BIOMES.find(b => b.id === biomeId)?.name || "Region"));
  };

  window.showNativeBanner = function (msg) {
    showBanner(msg);
    playSfx("unlock");
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
      wb.struct.visible = lvl > 0;
      wb.ring.material.color.setHex(lvl > 0 ? 0xfbbf24 : 0x10b981);
    });
  }

  // =========================================================================
  // 13. MAIN GAME LOOP
  // =========================================================================
  let prevTime = performance.now();
  let swingCooldown = 0;
  let swingAnimTimer = 0;
  let sellCooldown = 0;
  let passiveAccum = 0;
  let fullBagWarnCooldown = 0;
  let activeBiomeId = "forest";

  function tick(now) {
    requestAnimationFrame(tick);
    const dt = Math.min((now - prevTime) / 1000, 0.05);
    prevTime = now;

    // 1. Player Movement
    const speedBase = MOVE_SPEEDS[Math.min(state.moveSpeedLevel, 6) - 1];
    const speed = hasUniqueItem("golden_compass") ? speedBase * 1.15 : speedBase;
    const isMoving = Math.hypot(moveX, moveZ) > 0.05;
    if (isMoving) {
      const nx = playerGroup.position.x + moveX * speed * dt;
      const nz = playerGroup.position.z + moveZ * speed * dt;
      if (isWalkable(nx, playerGroup.position.z)) playerGroup.position.x = nx;
      if (isWalkable(playerGroup.position.x, nz)) playerGroup.position.z = nz;

      const targetYaw = Math.atan2(moveX, moveZ);
      let diff = targetYaw - playerGroup.rotation.y;
      while (diff > Math.PI) diff -= Math.PI * 2;
      while (diff < -Math.PI) diff += Math.PI * 2;
      playerGroup.rotation.y += diff * Math.min(1, dt * 14);

      const gait = Math.sin(now * 0.015) * 0.55;
      legL.rotation.x = gait;
      legR.rotation.x = -gait;
      armL.rotation.x = -gait * 0.7;
      playerBodyPivot.position.y = Math.abs(Math.sin(now * 0.015)) * 0.09;
    } else {
      legL.rotation.x *= 0.8;
      legR.rotation.x *= 0.8;
      armL.rotation.x *= 0.8;
      playerBodyPivot.position.y *= 0.8;
    }

    // 2. Smooth Third-Person Camera & Shadow Tracking
    const px = playerGroup.position.x;
    const pz = playerGroup.position.z;
    const desiredCamX = px;
    const desiredCamY = 14.5;
    const desiredCamZ = pz + 13.5;
    const camLerp = 1 - Math.exp(-7.5 * dt);
    camera.position.x += (desiredCamX - camera.position.x) * camLerp;
    camera.position.y += (desiredCamY - camera.position.y) * camLerp;
    camera.position.z += (desiredCamZ - camera.position.z) * camLerp;
    camera.lookAt(px, 0.9, pz - 1.5);

    sunLight.position.set(px + 14, 24, pz + 12);
    sunLight.target.position.set(px, 0, pz);

    // 3. Biome Atmosphere Transition
    const bNow = currentBiomeAt(px, pz);
    if (bNow.id !== activeBiomeId) {
      activeBiomeId = bNow.id;
      biomePillEl.textContent = bNow.name;
    }
    const targetSky = new THREE.Color(bNow.sky);
    currentSkyColor.lerp(targetSky, dt * 2.5);
    scene.fog.color.copy(currentSkyColor);

    // 4. Auto-Gather Nearest Resource Node
    swingCooldown = Math.max(0, swingCooldown - dt);
    fullBagWarnCooldown = Math.max(0, fullBagWarnCooldown - dt);
    let nearestNode = null;
    let nearestDist = 2.65;
    for (const node of resourceNodes) {
      if (node.respawnTimer > 0) {
        node.respawnTimer -= dt;
        if (node.respawnTimer <= 0) {
          node.hp = node.maxHp;
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
        const wobble = 1 + Math.sin(node.bounceTimer * 35) * 0.12;
        node.mesh.scale.set(wobble, 2 - wobble, wobble);
      } else if (node.mesh.scale.x === 1) {
        node.mesh.scale.set(1, 1, 1);
      }

      if (!isBiomeUnlocked(node.biome)) continue;
      const dist = Math.hypot(node.x - px, node.z - pz);
      if (dist < nearestDist) {
        nearestDist = dist;
        nearestNode = node;
      }
    }

    if (nearestNode && swingCooldown <= 0) {
      const isTree = nearestNode.kind === "wood";
      axeHead.visible = isTree;
      pickHead.visible = !isTree;
      const toolLvl = isTree ? state.axeLevel : state.pickaxeLevel;
      const activeCol = TIER_COLORS[Math.min(toolLvl, 6) - 1];
      toolHeadMat.color.setHex(activeCol);

      if (getBagCount() >= getMaxBag()) {
        if (fullBagWarnCooldown <= 0) {
          spawnFloatingText(playerGroup.position, "BACKPACK FULL!", "#ef4444");
          fullBagWarnCooldown = 1.6;
        }
      } else {
        // Face node subtly if stationary
        if (!isMoving) {
          playerGroup.rotation.y = Math.atan2(nearestNode.x - px, nearestNode.z - pz);
        }
        const rate = GATHER_RATES[Math.min(state.gatherSpeedLevel, 6) - 1];
        swingCooldown = 0.52 / rate;
        swingAnimTimer = 0.24;

        const dmg = toolLvl;
        nearestNode.hp -= dmg;
        nearestNode.bounceTimer = 0.22;

        const rDef = RESOURCES[nearestNode.kind];
        playSfx(isTree ? "chop" : (nearestNode.kind === "crystal" || nearestNode.kind === "rare" ? "crystal" : "mine"));
        haptic("light");
        spawnBurst(nearestNode.x, 1.0, nearestNode.z, rDef.color, 5);

        if (nearestNode.hp <= 0) {
          nearestNode.respawnTimer = 4.2;
          nearestNode.mesh.visible = false;
          const yieldAmt = Math.min(1 + Math.floor((toolLvl - 1) / 2), getMaxBag() - getBagCount());
          if (yieldAmt > 0) {
            state.inventory[nearestNode.kind] = (state.inventory[nearestNode.kind] || 0) + yieldAmt;
            state.gatheredTotals[nearestNode.kind] = (state.gatheredTotals[nearestNode.kind] || 0) + yieldAmt;
            stateDirty = true;
            refreshCharacterVisuals();
            playSfx("pickup");
            spawnFloatingText(nearestNode.mesh.position, `+${yieldAmt} ${rDef.name}`, rDef.css);
            spawnFlyingDrop(
              new THREE.Vector3(nearestNode.x, 1.0, nearestNode.z),
              playerGroup.position,
              rDef.color
            );
          }
        }
      }
    }

    // Tool Swing Animation
    if (swingAnimTimer > 0) {
      swingAnimTimer -= dt;
      armRPivot.rotation.x = -1.6 * Math.sin((swingAnimTimer / 0.24) * Math.PI);
    } else {
      armRPivot.rotation.x = isMoving ? Math.sin(now * 0.015) * 0.35 : 0;
    }

    // 5. Auto-Sell Zone
    sellCooldown = Math.max(0, sellCooldown - dt);
    coinSign.rotation.z += dt * 2.2;
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
        spawnFlyingDrop(
          new THREE.Vector3(px, 1.4, pz),
          new THREE.Vector3(SELL_ZONE.x, 1.1, SELL_ZONE.z),
          RESOURCES[k].color
        );
      }
    }

    // 6. Auto-Unlock Biome Gates on Walk-Up Pad
    for (const id in biomeGates) {
      const g = biomeGates[id];
      if (isBiomeUnlocked(id)) continue;
      const dGate = Math.hypot(px - g.biome.gate.x, pz - g.biome.gate.z);
      if (dGate < 2.6 && state.coins >= g.biome.unlockCost) {
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
          spawnBurst(g.biome.gate.x, 1.5, g.biome.gate.z, g.biome.accent, 14);
          showBanner(`${g.biome.name} Unlocked!`);
          syncToAndroid(true);
        }
      } else {
        g.charge = 0;
        g.padRing.scale.set(1, 1, 1);
      }
    }

    // 7. Auto-Build / Upgrade Pad Interaction & Spinner Animation
    for (const id in worldBuildings) {
      const wb = worldBuildings[id];
      const lvl = state.buildingLevels[id] || 0;
      if (lvl > 0) {
        wb.spinner.rotation.y += dt * (1.8 + lvl * 0.45);
      }
      if (!isBiomeUnlocked(wb.def.biome)) continue;
      if (lvl >= 5) continue;
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
          playSfx("unlock");
          haptic("upgrade");
          spawnBurst(wb.def.x, 1.5, wb.def.z, 0xfbbf24, 12);
          showBanner(`${wb.def.title} Lv.${lvl + 1}!`);
          syncToAndroid(true);
        }
      } else {
        wb.charge = 0;
      }
    }

    // 8. Passive Income Tick
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

    // 9. Animate Particles, Flying Drops & Floating Badges
    for (let i = particles.length - 1; i >= 0; i--) {
      const p = particles[i];
      p.age += dt;
      if (p.age >= p.life) {
        scene.remove(p.mesh);
        particles.splice(i, 1);
        continue;
      }
      p.vy -= 14 * dt;
      p.mesh.position.x += p.vx * dt;
      p.mesh.position.y = Math.max(0.1, p.mesh.position.y + p.vy * dt);
      p.mesh.position.z += p.vz * dt;
      const sc = 1 - p.age / p.life;
      p.mesh.scale.set(0.16 * sc, 0.16 * sc, 0.16 * sc);
    }

    for (let i = flyingDrops.length - 1; i >= 0; i--) {
      const f = flyingDrops[i];
      f.t += dt;
      const u = Math.min(1, f.t / f.dur);
      const tx = f.target.x;
      const ty = (f.target.y || 1.1);
      const tz = f.target.z;
      f.mesh.position.x = f.start.x + (tx - f.start.x) * u;
      f.mesh.position.z = f.start.z + (tz - f.start.z) * u;
      f.mesh.position.y = f.start.y + (ty - f.start.y) * u + Math.sin(u * Math.PI) * 1.4;
      f.mesh.rotation.y += dt * 10;
      if (u >= 1) {
        scene.remove(f.mesh);
        flyingDrops.splice(i, 1);
      }
    }

    const hw = window.innerWidth / 2;
    const hh = window.innerHeight / 2;
    const proj = new THREE.Vector3();
    for (let i = floatingBadges.length - 1; i >= 0; i--) {
      const b = floatingBadges[i];
      b.age += dt;
      if (b.age >= b.life) {
        b.el.remove();
        floatingBadges.splice(i, 1);
        continue;
      }
      proj.set(b.wx, b.wy + b.age * 1.6, b.wz);
      proj.project(camera);
      const sx = (proj.x * hw) + hw;
      const sy = -(proj.y * hh) + hh;
      b.el.style.left = sx + "px";
      b.el.style.top = sy + "px";
      b.el.style.opacity = String(1 - Math.pow(b.age / b.life, 2));
    }

    // 10. Sync to Android Kotlin HUD
    syncToAndroid(false);
    renderer.render(scene, camera);
  }

  // Boot ready
  refreshCharacterVisuals();
  refreshWorldState();
  document.getElementById("loading-overlay").classList.add("hidden");
  if (window.AndroidBridge && window.AndroidBridge.onEngineReady) {
    try { window.AndroidBridge.onEngineReady(); } catch (_) {}
  }
  requestAnimationFrame(tick);
})();
