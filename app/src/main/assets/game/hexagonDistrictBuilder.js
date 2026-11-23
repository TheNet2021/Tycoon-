(function () {
  "use strict";

  // =========================================================================
  // KENNEY HEXAGON KIT DISTRICT BUILDER (MAPS 2-12 ONLY — MAP 1 UNTOUCHED)
  // Places coherent, snap-aligned Hexagon Kit settlements & biome outposts
  // using instanced static terrain/river/path tiles and dynamic LOD/GLB slots
  // synchronized with live Android SettlementState (housing & 6 facilities).
  // =========================================================================

  const HEX_S = 2.6;
  const COL_DX = HEX_S;
  const ROW_DZ = HEX_S * 0.86602540378;
  const ODD_OFFSET = HEX_S * 0.5;
  const TILE_BASE_Y = -0.52; // 0.20 * 2.6 = 0.52 plinth -> deck flush at y = 0.00
  const DIRT_Y = -0.26;      // dirt.glb is 0.10 tall (0.26 scaled) -> deck flush at y = 0.00
  const PATH_Y = 0.0015;     // sits cleanly atop base plinth top (y = 0.00)
  const STEP_60 = Math.PI / 3;

  // Pointy-top odd-r offset cell -> world XZ
  function hexToWorld(ox, oz, c, r) {
    const parity = ((r % 2) + 2) % 2;
    return {
      x: ox + c * COL_DX + (parity === 1 ? ODD_OFFSET : 0),
      z: oz + r * ROW_DZ
    };
  }

  function hexRy(dirIndex) {
    return (dirIndex || 0) * STEP_60;
  }

  // Enclave definitions (strictly outside Map 1 forest and outside parent 30x30 playable slabs)
  const ENCLAVES = {
    mountains: {
      id: "hex_settlement_sanctuary",
      biome: "mountains",
      ox: -27.5,
      oz: -34.0,
      hasLiveSettlement: true,
      causeway: { minX: -18.6, maxX: -14.8, minZ: -35.6, maxZ: -32.4, cx: -16.4, cz: -34.0, w: 3.4, d: 2.8 }
    },
    jungle: {
      id: "hex_jungle_river_outpost",
      biome: "jungle",
      ox: 57.5,
      oz: -34.0,
      hasLiveSettlement: false,
      causeway: { minX: 48.8, maxX: 56.6, minZ: -35.6, maxZ: -32.4, cx: 52.7, cz: -34.0, w: 7.8, d: 2.6 }
    },
    crystal_desert: {
      id: "hex_crystal_caravanserai",
      biome: "crystal_desert",
      ox: 89.5,
      oz: 0.0,
      hasLiveSettlement: false,
      causeway: { minX: 82.8, maxX: 88.6, minZ: -1.6, maxZ: 1.6, cx: 85.7, cz: 0.0, w: 5.8, d: 2.6 }
    },
    sky_islands: {
      id: "hex_sky_windmill_terrace",
      biome: "sky_islands",
      ox: -22.5,
      oz: -68.0,
      hasLiveSettlement: false,
      causeway: { minX: -18.5, maxX: -14.8, minZ: -69.6, maxZ: -66.4, cx: -16.4, cz: -68.0, w: 3.4, d: 2.6 }
    },
    frozen_mountains: {
      id: "hex_quarry_bastion",
      biome: "frozen_mountains",
      ox: -89.5,
      oz: 0.0,
      hasLiveSettlement: false,
      causeway: { minX: -85.6, maxX: -82.8, minZ: -1.6, maxZ: 1.6, cx: -84.0, cz: 0.0, w: 3.2, d: 2.6 }
    }
  };

  // Dynamic settlement slots in Map 2 Hexagon Sanctuary
  const HOUSING_SLOTS = [
    { c: -1, r: -1, ry: hexRy(5) },
    { c: -2, r: 0,  ry: hexRy(0) },
    { c: -1, r: 1,  ry: hexRy(1) },
    { c: -2, r: -1, ry: hexRy(5) },
    { c: -2, r: 1,  ry: hexRy(1) },
    { c: 0,  r: 2,  ry: hexRy(1) }
  ];

  const FACILITY_SLOTS = {
    market:    { c: 2, r: -1, ry: hexRy(4), baseTile: "dirt" },
    farm:      { c: 1, r: 1,  ry: hexRy(2), baseTile: "dirt" },
    mill:      { c: 1, r: -1, ry: hexRy(5), baseTile: "grass" },
    watermill: { c: 3, r: 1,  ry: hexRy(3), baseTile: "grass" },
    mine:      { c: 0, r: -1, ry: hexRy(5), baseTile: "stone" },
    smelter:   { c: 1, r: -2, ry: hexRy(4), baseTile: "stone" }
  };

  // Runtime registries
  const walkableTilesByBiome = {};  // biomeId -> [{ x, z, r }]
  const solidCollidersByBiome = {}; // biomeId -> [{ x, z, r }]
  const animatedRotors = [];        // { biome, mesh, axis, speed }
  const pulsingMarkers = [];        // { biome, mesh, baseScale }
  const causeways = [];             // { biome, mesh, spec }

  let cachedTHREE = null;
  let cachedMgr = null;
  let settlementRoot = null;
  let sanctuaryReady = false;
  let latestSettlementState = null;

  // Build static tile layout for Map 2 Hexagon Settlement Sanctuary
  function getSanctuaryLayout() {
    // Base tiles (grass, grass-forest, grass-hill, stone, stone-hill, stone-mountain, dirt, dirt-lumber, water, water-island, river-*, bridge)
    const bases = [
      // Main boulevard & approach from Mountains West edge
      { c: 4, r: 0,  model: "grass", walkable: true, path: { model: "path-straight", ry: 0 } },
      { c: 3, r: 0,  model: "bridge", ry: hexRy(1), walkable: true }, // River NE-SW under E-W bridge!
      { c: 2, r: 0,  model: "grass", walkable: true, path: { model: "path-square", ry: 0 } },
      { c: 1, r: 0,  model: "grass", walkable: true, path: { model: "path-crossing", ry: 0 } },
      { c: 0, r: 0,  model: "grass", walkable: true, path: { model: "path-intersectionC", ry: hexRy(3) } },
      { c: -1, r: 0, model: "grass", walkable: true, path: { model: "path-square-end", ry: hexRy(3) } },

      // River channel flowing NE -> SW through (3,0) bridge into South-East pond
      { c: 4, r: -2, model: "river-end", ry: hexRy(1), walkable: false }, // exits SW
      { c: 3, r: -1, model: "river-straight", ry: hexRy(1), walkable: false },
      { c: 2, r: 1,  model: "river-straight", ry: hexRy(1), walkable: false },
      { c: 2, r: 2,  model: "river-corner-sharp", ry: hexRy(4), walkable: false }, // NE(1) -> E(0)
      { c: 3, r: 2,  model: "water-island", ry: hexRy(2), walkable: false },
      { c: 4, r: 1,  model: "water", walkable: false },

      // Quarry / Mountain ridge & Timber grove framing the enclave
      { c: 0, r: -2, model: "stone-mountain", ry: hexRy(0), walkable: false },
      { c: 2, r: -2, model: "stone-hill", ry: hexRy(4), walkable: false },
      { c: -1, r: -2, model: "grass-forest", ry: hexRy(1), walkable: false },
      { c: -3, r: 0,  model: "grass-hill", ry: hexRy(2), walkable: false },
      { c: -3, r: -1, model: "grass-forest", ry: hexRy(4), walkable: false },
      { c: -3, r: 1,  model: "grass-forest", ry: hexRy(0), walkable: false },
      { c: -1, r: 2,  model: "dirt-lumber", ry: hexRy(3), walkable: false },
      { c: 1, r: 2,  model: "grass-forest", ry: hexRy(2), walkable: false }
    ];
    return bases;
  }

  // Static coherent outpost layouts for Maps 6, 7, 8, 10
  function getOutpostLayouts() {
    return {
      jungle: {
        bases: [
          { c: 0, r: 0, model: "grass", walkable: true, path: { model: "path-start", ry: hexRy(3) } },
          { c: 1, r: 0, model: "bridge", ry: hexRy(1), walkable: true },
          { c: 2, r: 0, model: "grass", walkable: true, path: { model: "path-end", ry: 0 } },
          { c: 1, r: -1, model: "river-end", ry: hexRy(1), walkable: false },
          { c: 0, r: 1,  model: "river-corner-sharp", ry: hexRy(4), walkable: false },
          { c: 1, r: 1,  model: "water-island", ry: hexRy(1), walkable: false },
          { c: 2, r: -1, model: "grass-forest", ry: hexRy(2), walkable: false },
          { c: -1, r: 0, model: "dirt-lumber", ry: hexRy(0), walkable: false }
        ],
        buildings: [
          { c: 2, r: 1,  model: "building-watermill", ry: hexRy(2) },
          { c: 0, r: -1, model: "building-cabin", ry: hexRy(5) },
          { c: 3, r: 0,  model: "building-farm", ry: hexRy(3) }
        ]
      },
      crystal_desert: {
        bases: [
          { c: 0, r: 0, model: "sand", walkable: true, path: { model: "path-start", ry: hexRy(3) } },
          { c: 1, r: 0, model: "sand", walkable: true, path: { model: "path-square", ry: 0 } },
          { c: 2, r: 0, model: "sand", walkable: true, path: { model: "path-end", ry: 0 } },
          { c: 1, r: 1, model: "water-island", ry: hexRy(0), walkable: false },
          { c: 2, r: 1, model: "water", walkable: false },
          { c: 0, r: -1, model: "sand-desert", ry: hexRy(1), walkable: false },
          { c: 2, r: -1, model: "sand-desert", ry: hexRy(4), walkable: false }
        ],
        buildings: [
          { c: 1, r: -1, model: "building-market", ry: hexRy(5) },
          { c: 3, r: 0,  model: "building-village", ry: hexRy(3) }
        ]
      },
      sky_islands: {
        bases: [
          { c: 2, r: 0, model: "grass", walkable: true, path: { model: "path-straight", ry: 0 } },
          { c: 1, r: 0, model: "grass", walkable: true, path: { model: "path-square", ry: 0 } },
          { c: 0, r: 0, model: "grass", walkable: true, path: { model: "path-end", ry: hexRy(3) } },
          { c: 0, r: 1, model: "grass-hill", ry: hexRy(2), walkable: false },
          { c: 2, r: -1, model: "stone-hill", ry: hexRy(5), walkable: false },
          { c: 2, r: 1, model: "water-island", ry: hexRy(3), walkable: false }
        ],
        buildings: [
          { c: 1, r: -1, model: "building-mill", ry: hexRy(4) },
          { c: 1, r: 1,  model: "building-house", ry: hexRy(1) }
        ]
      },
      frozen_mountains: {
        bases: [
          { c: 2, r: 0, model: "stone", walkable: true, path: { model: "path-straight", ry: 0 } },
          { c: 1, r: 0, model: "stone", walkable: true, path: { model: "path-straight", ry: 0 } },
          { c: 0, r: 0, model: "stone", walkable: true, path: { model: "path-end", ry: hexRy(3) } },
          { c: 0, r: -1, model: "stone-mountain", ry: hexRy(1), walkable: false },
          { c: 2, r: -1, model: "stone-hill", ry: hexRy(4), walkable: false },
          { c: 1, r: 1,  model: "dirt", yOffset: 0.26, walkable: true }
        ],
        buildings: [
          { c: 1, r: -1, model: "building-mine", ry: hexRy(5) },
          { c: 0, r: 1,  model: "building-smelter", ry: hexRy(1) }
        ]
      }
    };
  }

  function addUnderSkirtFoundation(THREE, chunkGrp, enc, tiles, colorHex, pbrMat) {
    // Subterranean rock plinth directly beneath hex tiles so zero floating underside gap is visible
    let minX = Infinity, maxX = -Infinity, minZ = Infinity, maxZ = -Infinity;
    for (let i = 0; i < tiles.length; i++) {
      const p = hexToWorld(enc.ox, enc.oz, tiles[i].c, tiles[i].r);
      if (p.x < minX) minX = p.x;
      if (p.x > maxX) maxX = p.x;
      if (p.z < minZ) minZ = p.z;
      if (p.z > maxZ) maxZ = p.z;
    }
    const cx = (minX + maxX) * 0.5;
    const cz = (minZ + maxZ) * 0.5;
    const w = Math.max(4.2, (maxX - minX) + 1.2);
    const d = Math.max(4.2, (maxZ - minZ) + 1.2);
    const skirt = new THREE.Mesh(
      new THREE.CylinderGeometry(Math.max(w, d) * 0.48, Math.max(w, d) * 0.52, 0.72, 6),
      pbrMat(colorHex, { roughness: 0.92, metalness: 0.04 })
    );
    skirt.position.set(cx, -0.84, cz);
    skirt.scale.set(w / Math.max(w, d), 1, d / Math.max(w, d));
    skirt.receiveShadow = true;
    chunkGrp.add(skirt);
  }

  function registerWalkableHex(biomeId, x, z, radius) {
    if (!walkableTilesByBiome[biomeId]) walkableTilesByBiome[biomeId] = [];
    walkableTilesByBiome[biomeId].push({ x, z, r: radius || 1.42 });
  }

  function buildStaticHexTiles(mgr, THREE, chunkGrp, enc, tiles) {
    const HEX = mgr.HEX;
    const baseGroups = {};
    const pathGroups = {};
    const needed = new Set();

    for (let i = 0; i < tiles.length; i++) {
      const t = tiles[i];
      needed.add(t.model);
      if (t.path) needed.add(t.path.model);
    }

    return Promise.all([...needed].map((name) => mgr.loadModel(THREE, HEX + name + ".glb").then((asset) => [name, asset]))).then((pairs) => {
      const map = Object.fromEntries(pairs);
      for (let i = 0; i < tiles.length; i++) {
        const t = tiles[i];
        const pos = hexToWorld(enc.ox, enc.oz, t.c, t.r);
        if (!baseGroups[t.model]) baseGroups[t.model] = [];
        baseGroups[t.model].push({
          x: pos.x,
          y: TILE_BASE_Y + (t.yOffset || 0),
          z: pos.z,
          ry: t.ry || 0
        });
        if (t.path) {
          if (!pathGroups[t.path.model]) pathGroups[t.path.model] = [];
          pathGroups[t.path.model].push({
            x: pos.x,
            y: PATH_Y,
            z: pos.z,
            ry: t.path.ry || 0
          });
        }
      }
      for (const name in baseGroups) {
        if (map[name]) mgr.buildInstancedHexTiles(THREE, chunkGrp, map[name], baseGroups[name], HEX_S);
      }
      for (const name in pathGroups) {
        if (map[name]) mgr.buildInstancedHexTiles(THREE, chunkGrp, map[name], pathGroups[name], HEX_S);
      }
    });
  }

  function addTierBadgeAndGlow(THREE, parentGrp, level, accentHex) {
    if (level <= 0) return;
    // Clean hexagonal stone/gold plinth collar & floating tier crystal for functional upgrade feedback
    const ringGeo = new THREE.RingGeometry(0.95, 1.16, 6);
    const ringCol = level >= 3 ? 0xfbbf24 : (level === 2 ? 0x38bdf8 : (accentHex || 0x10b981));
    const ringMat = new THREE.MeshBasicMaterial({ color: ringCol, side: THREE.DoubleSide, transparent: true, opacity: 0.72 });
    const ring = new THREE.Mesh(ringGeo, ringMat);
    ring.rotation.x = -Math.PI / 2;
    ring.position.y = 0.025;
    parentGrp.add(ring);

    if (level >= 2) {
      for (let pi = 0; pi < Math.min(level - 1, 2); pi++) {
        const gem = new THREE.Mesh(
          new THREE.OctahedronGeometry(0.11, 0),
          new THREE.MeshStandardMaterial({ color: ringCol, emissive: ringCol, emissiveIntensity: 0.75, roughness: 0.25 })
        );
        gem.position.set(pi === 0 ? -0.82 : 0.82, 0.42, 0.72);
        parentGrp.add(gem);
      }
    }
  }

  function addEmptySurveyPlot(THREE, parentGrp, x, z, accentHex, pulsingList, biomeId) {
    const plot = new THREE.Group();
    plot.position.set(x, 0.015, z);
    const ring = new THREE.Mesh(
      new THREE.RingGeometry(0.72, 0.92, 6),
      new THREE.MeshBasicMaterial({ color: accentHex || 0x34d399, side: THREE.DoubleSide, transparent: true, opacity: 0.55 })
    );
    ring.rotation.x = -Math.PI / 2;
    plot.add(ring);

    // 4 timber survey corner stakes
    const stakeGeo = new THREE.CylinderGeometry(0.04, 0.05, 0.38, 6);
    const stakeMat = new THREE.MeshStandardMaterial({ color: 0xb45309, roughness: 0.85 });
    for (let i = 0; i < 4; i++) {
      const ang = (i / 4) * Math.PI * 2 + Math.PI * 0.25;
      const stake = new THREE.Mesh(stakeGeo, stakeMat);
      stake.position.set(Math.cos(ang) * 0.72, 0.19, Math.sin(ang) * 0.72);
      stake.castShadow = true;
      plot.add(stake);
    }
    parentGrp.add(plot);
    pulsingList.push({ biome: biomeId, mesh: ring, baseScale: 1.0 });
  }

  function renderSanctuarySettlement(settlement) {
    latestSettlementState = settlement || latestSettlementState;
    if (!sanctuaryReady || !settlementRoot || !cachedMgr || !cachedTHREE) return;

    const THREE = cachedTHREE;
    const mgr = cachedMgr;
    const HEX = mgr.HEX;
    const enc = ENCLAVES.mountains;

    // Clear previous dynamic settlement children cleanly
    while (settlementRoot.children.length > 0) {
      settlementRoot.remove(settlementRoot.children[0]);
    }
    for (let i = animatedRotors.length - 1; i >= 0; i--) {
      if (animatedRotors[i].biome === "mountains") animatedRotors.splice(i, 1);
    }
    for (let i = pulsingMarkers.length - 1; i >= 0; i--) {
      if (pulsingMarkers[i].biome === "mountains") pulsingMarkers.splice(i, 1);
    }
    solidCollidersByBiome.mountains = [];

    const sState = latestSettlementState || { housingUnits: [], facilities: [] };
    const housingUnits = Array.isArray(sState.housingUnits) ? sState.housingUnits : [];
    const facilities = Array.isArray(sState.facilities) ? sState.facilities : [];
    const facLevelById = {};
    for (let i = 0; i < facilities.length; i++) {
      facLevelById[facilities[i].typeId] = facilities[i].level || 0;
    }

    const urlsNeeded = new Set(["grass.glb", "dirt.glb", "stone.glb"]);
    for (let i = 0; i < Math.min(housingUnits.length, HOUSING_SLOTS.length); i++) {
      const hu = housingUnits[i];
      if (hu.isCompleted) {
        urlsNeeded.add((hu.hexModel || "building-cabin") + ".glb");
      }
    }
    const FAC_MODELS = {
      market: "building-market.glb",
      farm: "building-farm.glb",
      mill: "building-mill.glb",
      watermill: "building-watermill.glb",
      mine: "building-mine.glb",
      smelter: "building-smelter.glb"
    };
    for (const fid in FACILITY_SLOTS) {
      if ((facLevelById[fid] || 0) > 0 && FAC_MODELS[fid]) {
        urlsNeeded.add(FAC_MODELS[fid]);
      }
    }

    Promise.all([...urlsNeeded].map((u) => mgr.loadModel(THREE, HEX + u).then((a) => [u, a]))).then((pairs) => {
      const assets = Object.fromEntries(pairs);
      // Guard against stale async callback
      while (settlementRoot.children.length > 0) {
        settlementRoot.remove(settlementRoot.children[0]);
      }

      // A. Render 6 Housing Lots
      for (let i = 0; i < HOUSING_SLOTS.length; i++) {
        const slot = HOUSING_SLOTS[i];
        const pos = hexToWorld(enc.ox, enc.oz, slot.c, slot.r);
        const hu = i < housingUnits.length ? housingUnits[i] : null;

        if (!hu) {
          // Empty reserved grass plot
          if (assets["grass.glb"]) {
            const tile = mgr.cloneHexTile(THREE, assets["grass.glb"], HEX_S, pos.x, TILE_BASE_Y, pos.z, 0);
            if (tile) settlementRoot.add(tile);
          }
          addEmptySurveyPlot(THREE, settlementRoot, pos.x, pos.z, 0x34d399, pulsingMarkers, "mountains");
        } else if (!hu.isCompleted) {
          // Under construction: dirt pad + timber framing stakes + beacon
          if (assets["dirt.glb"]) {
            const tile = mgr.cloneHexTile(THREE, assets["dirt.glb"], HEX_S, pos.x, DIRT_Y, pos.z, 0);
            if (tile) settlementRoot.add(tile);
          }
          addEmptySurveyPlot(THREE, settlementRoot, pos.x, pos.z, 0xfbbf24, pulsingMarkers, "mountains");
          solidCollidersByBiome.mountains.push({ x: pos.x, z: pos.z, r: 0.95 });
        } else {
          // Completed house/cabin/village GLB sitting on its own hex plinth + upgrade ring
          const modelFile = (hu.hexModel || "building-cabin") + ".glb";
          const asset = assets[modelFile] || assets["building-cabin.glb"];
          const lvl = Math.max(1, hu.upgradeLevel || 1);
          const scaleBoost = HEX_S * (1.0 + (lvl - 1) * 0.045);
          if (asset) {
            const tile = mgr.cloneHexTile(THREE, asset, scaleBoost, pos.x, TILE_BASE_Y, pos.z, slot.ry);
            if (tile) {
              settlementRoot.add(tile);
              const badgeHolder = new THREE.Group();
              badgeHolder.position.set(pos.x, 0, pos.z);
              addTierBadgeAndGlow(THREE, badgeHolder, lvl, 0x34d399);
              settlementRoot.add(badgeHolder);
            }
          }
          solidCollidersByBiome.mountains.push({ x: pos.x, z: pos.z, r: 1.05 });
        }
      }

      // B. Render 6 Civic & Production Facilities (farm, mill, watermill, market, mine, smelter)
      const FAC_ACCENTS = {
        market: 0xf59e0b,
        farm: 0x34d399,
        mill: 0xfbbf24,
        watermill: 0x38bdf8,
        mine: 0x94a3b8,
        smelter: 0xfb923c
      };

      for (const fid in FACILITY_SLOTS) {
        const slot = FACILITY_SLOTS[fid];
        const pos = hexToWorld(enc.ox, enc.oz, slot.c, slot.r);
        const lvl = facLevelById[fid] || 0;

        if (lvl <= 0) {
          const baseName = slot.baseTile + ".glb";
          const baseY = slot.baseTile === "dirt" ? DIRT_Y : TILE_BASE_Y;
          if (assets[baseName]) {
            const tile = mgr.cloneHexTile(THREE, assets[baseName], HEX_S, pos.x, baseY, pos.z, 0);
            if (tile) settlementRoot.add(tile);
          }
          addEmptySurveyPlot(THREE, settlementRoot, pos.x, pos.z, FAC_ACCENTS[fid], pulsingMarkers, "mountains");
        } else {
          const modelFile = FAC_MODELS[fid];
          const asset = assets[modelFile];
          const scaleBoost = HEX_S * (1.0 + (lvl - 1) * 0.045);
          if (asset) {
            const tile = mgr.cloneHexTile(THREE, asset, scaleBoost, pos.x, TILE_BASE_Y, pos.z, slot.ry);
            if (tile) {
              settlementRoot.add(tile);
              if (tile.userData && Array.isArray(tile.userData.animRotors)) {
                for (let ri = 0; ri < tile.userData.animRotors.length; ri++) {
                  const rot = tile.userData.animRotors[ri];
                  animatedRotors.push({
                    biome: "mountains",
                    mesh: rot.mesh,
                    axis: rot.axis,
                    speed: (fid === "watermill" ? 1.8 : 1.35) * (1 + (lvl - 1) * 0.35)
                  });
                }
              }
              const badgeHolder = new THREE.Group();
              badgeHolder.position.set(pos.x, 0, pos.z);
              addTierBadgeAndGlow(THREE, badgeHolder, lvl, FAC_ACCENTS[fid]);
              settlementRoot.add(badgeHolder);
            }
          }
          solidCollidersByBiome.mountains.push({ x: pos.x, z: pos.z, r: 1.08 });
        }
      }
    });
  }

  function initDistricts(THREE, getBiomeChunk, pbrMat) {
    cachedTHREE = THREE;
    if (!window.ModelAssetManager) return;
    cachedMgr = window.ModelAssetManager;

    const outpostLayouts = getOutpostLayouts();
    const sanctuaryTiles = getSanctuaryLayout();

    // Register causeways & walkable footprints synchronously so navigation is immediate once biome unlocks
    Object.keys(ENCLAVES).forEach((biomeId) => {
      if (biomeId === "forest") return; // Preserve Map 1 untouched
      const enc = ENCLAVES[biomeId];
      const chunkGrp = getBiomeChunk(biomeId);

      // Causeway ramp mesh connecting parent biome slab cleanly to hex district
      const cw = enc.causeway;
      const deckMat = pbrMat(biomeId === "crystal_desert" ? 0xd97706 : 0x64748b, { roughness: 0.85, metalness: 0.08 });
      const causewayMesh = new THREE.Mesh(
        new THREE.BoxGeometry(cw.w, 0.24, cw.d),
        deckMat
      );
      causewayMesh.position.set(cw.cx, -0.10, cw.cz);
      causewayMesh.receiveShadow = true;
      causewayMesh.castShadow = true;
      chunkGrp.add(causewayMesh);
      causeways.push({ biome: biomeId, mesh: causewayMesh, spec: cw });

      const tiles = biomeId === "mountains" ? sanctuaryTiles : outpostLayouts[biomeId].bases;
      const buildings = biomeId === "mountains" ? [] : outpostLayouts[biomeId].buildings;

      const allCoords = tiles.concat(buildings);
      if (biomeId === "mountains") {
        HOUSING_SLOTS.forEach((s) => allCoords.push(s));
        Object.values(FACILITY_SLOTS).forEach((s) => allCoords.push(s));
      }

      // Walkable hex tiles
      for (let i = 0; i < tiles.length; i++) {
        if (tiles[i].walkable) {
          const p = hexToWorld(enc.ox, enc.oz, tiles[i].c, tiles[i].r);
          registerWalkableHex(biomeId, p.x, p.z, 1.44);
        }
      }
      if (biomeId === "mountains") {
        // Housing & facility lots are also walkable up to building perimeter
        HOUSING_SLOTS.forEach((s) => {
          const p = hexToWorld(enc.ox, enc.oz, s.c, s.r);
          registerWalkableHex(biomeId, p.x, p.z, 1.38);
        });
        Object.values(FACILITY_SLOTS).forEach((s) => {
          const p = hexToWorld(enc.ox, enc.oz, s.c, s.r);
          registerWalkableHex(biomeId, p.x, p.z, 1.38);
        });
      } else {
        buildings.forEach((b) => {
          const p = hexToWorld(enc.ox, enc.oz, b.c, b.r);
          if (!solidCollidersByBiome[biomeId]) solidCollidersByBiome[biomeId] = [];
          solidCollidersByBiome[biomeId].push({ x: p.x, z: p.z, r: 1.05 });
        });
      }

      addUnderSkirtFoundation(
        THREE,
        chunkGrp,
        enc,
        allCoords,
        biomeId === "crystal_desert" ? 0x9c6122 : (biomeId === "frozen_mountains" ? 0x476b85 : 0x475569),
        pbrMat
      );

      // Register lazy Hexagon Kit GLB mount when this biome streams into view
      window.ModelAssetManager.registerBiomeSceneryHook(biomeId, (mgr, THREE_REF) => {
        const distGrp = new THREE_REF.Group();
        distGrp.name = enc.id;
        chunkGrp.add(distGrp);

        buildStaticHexTiles(mgr, THREE_REF, distGrp, enc, tiles).then(() => {
          if (biomeId === "mountains") {
            settlementRoot = new THREE_REF.Group();
            settlementRoot.name = "hex_live_settlement";
            distGrp.add(settlementRoot);
            sanctuaryReady = true;
            renderSanctuarySettlement(latestSettlementState);
          } else {
            // Mount outpost GLB buildings with animated rotors
            const bUrls = [...new Set(buildings.map((b) => mgr.HEX + b.model + ".glb"))];
            Promise.all(bUrls.map((u) => mgr.loadModel(THREE_REF, u).then((a) => [u, a]))).then((pairs) => {
              const map = Object.fromEntries(pairs);
              for (let i = 0; i < buildings.length; i++) {
                const b = buildings[i];
                const asset = map[mgr.HEX + b.model + ".glb"];
                if (!asset) continue;
                const p = hexToWorld(enc.ox, enc.oz, b.c, b.r);
                const tile = mgr.cloneHexTile(THREE_REF, asset, HEX_S, p.x, TILE_BASE_Y, p.z, b.ry || 0);
                if (tile) {
                  distGrp.add(tile);
                  if (tile.userData && Array.isArray(tile.userData.animRotors)) {
                    for (let ri = 0; ri < tile.userData.animRotors.length; ri++) {
                      const rot = tile.userData.animRotors[ri];
                      animatedRotors.push({ biome: biomeId, mesh: rot.mesh, axis: rot.axis, speed: 1.5 });
                    }
                  }
                }
              }
            });
          }
        });
      });
    });
  }

  function isWalkable(x, z, isUnlockedFn) {
    for (let ci = 0; ci < causeways.length; ci++) {
      const c = causeways[ci];
      if (!isUnlockedFn(c.biome)) continue;
      const s = c.spec;
      if (x >= s.minX && x <= s.maxX && z >= s.minZ && z <= s.maxZ) return true;
    }
    const keys = Object.keys(walkableTilesByBiome);
    for (let ki = 0; ki < keys.length; ki++) {
      const biomeId = keys[ki];
      if (!isUnlockedFn(biomeId)) continue;
      const list = walkableTilesByBiome[biomeId];
      let onWalkableHex = false;
      for (let i = 0; i < list.length; i++) {
        if (Math.hypot(x - list[i].x, z - list[i].z) <= list[i].r) {
          onWalkableHex = true;
          break;
        }
      }
      if (!onWalkableHex) continue;
      const cols = solidCollidersByBiome[biomeId] || [];
      for (let oi = 0; oi < cols.length; oi++) {
        if (Math.hypot(x - cols[oi].x, z - cols[oi].z) < cols[oi].r) return false;
      }
      return true;
    }
    return false;
  }

  function getFloorY(x, z) {
    const keys = Object.keys(walkableTilesByBiome);
    for (let ki = 0; ki < keys.length; ki++) {
      const list = walkableTilesByBiome[keys[ki]];
      for (let i = 0; i < list.length; i++) {
        if (Math.hypot(x - list[i].x, z - list[i].z) <= 1.6) return 0.08;
      }
    }
    return null;
  }

  function syncSettlement(settlement) {
    renderSanctuarySettlement(settlement);
  }

  function refreshCausewayVisibility(isUnlockedFn) {
    for (let i = 0; i < causeways.length; i++) {
      causeways[i].mesh.visible = Boolean(isUnlockedFn(causeways[i].biome));
    }
  }

  function updateAnimations(now, dt, visibleChunks) {
    for (let i = 0; i < animatedRotors.length; i++) {
      const r = animatedRotors[i];
      const cg = visibleChunks[r.biome];
      if (cg && !cg.visible) continue;
      if (r.mesh && r.mesh.rotation && r.axis) {
        r.mesh.rotation[r.axis] += dt * r.speed;
      }
    }
    for (let i = 0; i < pulsingMarkers.length; i++) {
      const pm = pulsingMarkers[i];
      const cg = visibleChunks[pm.biome];
      if (cg && !cg.visible) continue;
      const sc = pm.baseScale * (1 + Math.sin(now * 0.0045 + i) * 0.07);
      pm.mesh.scale.set(sc, sc, 1);
    }
  }

  window.HexagonDistrictBuilder = {
    ENCLAVES,
    initDistricts,
    syncSettlement,
    refreshCausewayVisibility,
    isWalkable,
    getFloorY,
    updateAnimations
  };
})();
