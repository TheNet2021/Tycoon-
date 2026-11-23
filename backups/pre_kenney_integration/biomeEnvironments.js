(function () {
  "use strict";

  // =========================================================================
  // BIOME ENVIRONMENTS BUILDER (MAPS 2-12 REWORK — MAP 1 PRESERVED)
  // Generates sculpted terrain heightfields, natural water/lava/mire features,
  // InstancedMesh biome flora & rock formations, atmospheric props, and
  // monumental multi-part landmarks for Biomes 2 through 12.
  // =========================================================================

  const animatedPropsByBiome = {};
  const biomePadsMap = {};

  function hash2(x, y) {
    const n = Math.sin(x * 127.1 + y * 311.7) * 43758.5453123;
    return n - Math.floor(n);
  }

  function smoothstep(edge0, edge1, x) {
    const t = Math.max(0, Math.min(1, (x - edge0) / (edge1 - edge0 || 1)));
    return t * t * (3 - 2 * t);
  }

  // Per-biome atmospheric lighting & fog tuning (Map 1 forest preserves default lighting)
  const BIOME_ATMOSPHERE = {
    forest:                { fogDensity: 0.0024, hemiSky: 0xd8f0ff, hemiGround: 0x3a4d32, sunCol: 0xfff1d6, sunInt: 1.38 },
    mountains:             { fogDensity: 0.0031, hemiSky: 0xd0e4f7, hemiGround: 0x4b5563, sunCol: 0xfff5e8, sunInt: 1.35 },
    desert:                { fogDensity: 0.0028, hemiSky: 0xffe4b5, hemiGround: 0x8c5a2b, sunCol: 0xffe3a8, sunInt: 1.50 },
    frozen:                { fogDensity: 0.0032, hemiSky: 0xe0f4ff, hemiGround: 0x628ea8, sunCol: 0xeef9ff, sunInt: 1.40 },
    volcanic:              { fogDensity: 0.0044, hemiSky: 0x8c3b32, hemiGround: 0x2b1412, sunCol: 0xff9a66, sunInt: 1.28 },
    jungle:                { fogDensity: 0.0036, hemiSky: 0x99f6e4, hemiGround: 0x1b4d3e, sunCol: 0xecfccb, sunInt: 1.34 },
    crystal_desert:        { fogDensity: 0.0031, hemiSky: 0xfbcfe8, hemiGround: 0x6b3a6e, sunCol: 0xffe4f2, sunInt: 1.44 },
    sky_islands:           { fogDensity: 0.0020, hemiSky: 0xe0f7ff, hemiGround: 0x3f8f7c, sunCol: 0xfffbeb, sunInt: 1.48 },
    toxic_swamp:           { fogDensity: 0.0048, hemiSky: 0xa3b86c, hemiGround: 0x243319, sunCol: 0xd9f99d, sunInt: 1.24 },
    frozen_mountains:      { fogDensity: 0.0034, hemiSky: 0xcffafe, hemiGround: 0x476b85, sunCol: 0xe0f2fe, sunInt: 1.42 },
    volcanic_lands:        { fogDensity: 0.0048, hemiSky: 0x7f1d1d, hemiGround: 0x1f0f0e, sunCol: 0xfb7185, sunInt: 1.26 },
    ancient_machine_world: { fogDensity: 0.0034, hemiSky: 0x7dd3fc, hemiGround: 0x1e293b, sunCol: 0xbae6fd, sunInt: 1.32 }
  };

  // Audited non-overlapping Water / Lava / Mire / Coolant basin coordinates in biome-local space (lx, lz)
  const BASIN_SPECS = {
    mountains:             { lx: 6.4,  lz: -5.8, rx: 3.4, rz: 2.9, kind: "alpine_lake" },
    desert:                { lx: -7.5, lz: 5.4,  rx: 3.2, rz: 2.5, kind: "oasis" },
    frozen:                { lx: 6.5,  lz: 5.5,  rx: 3.3, rz: 2.7, kind: "ice_pond" },
    volcanic:              { lx: -5.8, lz: 8.2,  rx: 3.1, rz: 2.7, kind: "lava_pool" },
    jungle:                { lx: -6.2, lz: 5.8,  rx: 3.3, rz: 2.9, kind: "jungle_lagoon" },
    crystal_desert:        { lx: -6.0, lz: 5.4,  rx: 3.1, rz: 2.6, kind: "crystal_pool" },
    sky_islands:           { lx: -5.8, lz: 6.0,  rx: 3.1, rz: 2.7, kind: "celestial_pond" },
    toxic_swamp:           { lx: 6.2,  lz: -5.8, rx: 3.2, rz: 2.8, kind: "toxic_mire" },
    frozen_mountains:      { lx: 6.2,  lz: -5.8, rx: 3.1, rz: 2.7, kind: "glacial_tarn" },
    volcanic_lands:        { lx: -5.8, lz: -6.2, rx: 3.1, rz: 2.6, kind: "magma_lake" },
    ancient_machine_world: { lx: -5.6, lz: -5.6, rx: 2.9, rz: 2.5, kind: "coolant_basin" }
  };

  function setBiomePads(biomeId, pads) {
    biomePadsMap[biomeId] = pads || [];
  }

  function computeFlatMask(biomeId, lx, lz, pads) {
    const dAxisX = Math.abs(lx);
    const dAxisZ = Math.abs(lz);
    const roadClear = Math.min(smoothstep(1.9, 3.6, dAxisX), smoothstep(1.9, 3.6, dAxisZ));
    const centerDist = Math.hypot(lx, lz);
    const centerMask = smoothstep(3.2, 5.8, centerDist);

    let objMask = 1.0;
    for (let i = 0; i < pads.length; i++) {
      const p = pads[i];
      const d = Math.hypot(lx - p.lx, lz - p.lz);
      const m = smoothstep(p.r * 0.72, p.r * 1.45, d);
      if (m < objMask) objMask = m;
    }
    return roadClear * centerMask * objMask;
  }

  function sampleBiomeTerrainLocalY(biomeId, lx, lz, bi, pads) {
    if (biomeId === "forest") return 0;
    const effectivePads = pads || biomePadsMap[biomeId] || [];
    const edge = Math.max(Math.abs(lx), Math.abs(lz));
    const flatMask = computeFlatMask(biomeId, lx, lz, effectivePads);
    const dAxisMin = Math.min(Math.abs(lx), Math.abs(lz));
    const bridgeCorridor = smoothstep(2.6, 5.2, dAxisMin);

    const basin = BASIN_SPECS[biomeId];
    let basinDepth = 0;
    if (basin) {
      const bx = (lx - basin.lx) / basin.rx;
      const bz = (lz - basin.lz) / basin.rz;
      const bd = Math.hypot(bx, bz);
      if (bd < 1.0) {
        basinDepth = -0.36 * (1 - bd * bd);
      }
    }

    const rimT = smoothstep(10.8, 15.2, edge) * bridgeCorridor;
    const cornerT = smoothstep(11.5, 16.5, Math.hypot(lx, lz)) * bridgeCorridor;

    const n1 = Math.sin(lx * 0.34 + bi * 1.3) * Math.cos(lz * 0.31 - bi * 0.9);
    const n2 = Math.sin((lx - lz) * 0.58 + bi * 2.1);

    let rawLift = 0;
    switch (biomeId) {
      case "mountains":
      case "frozen_mountains":
        rawLift = rimT * (1.85 + Math.abs(n1) * 1.55) + cornerT * 0.95 + Math.max(0, n1 * 0.42 + n2 * 0.18);
        break;
      case "desert":
      case "crystal_desert":
        rawLift = rimT * 1.45 + (Math.sin(lx * 0.28 + lz * 0.18 + bi) * 0.5 + 0.5) * 0.52 + cornerT * 0.7;
        break;
      case "frozen":
        rawLift = rimT * (1.15 + Math.abs(n1) * 0.75) + Math.max(0, n1 * 0.35 + 0.12);
        break;
      case "volcanic":
      case "volcanic_lands":
        rawLift = rimT * (2.1 + Math.abs(n2) * 1.4) + cornerT * 1.05 + Math.max(0, Math.abs(n1) * 0.55 - 0.12);
        break;
      case "jungle": {
        const jRaw = rimT * 1.65 + Math.max(0, n1 * 0.5 + 0.18);
        rawLift = Math.round(jRaw * 2.6) / 2.6;
        break;
      }
      case "sky_islands":
        rawLift = rimT * 1.1 + Math.max(0, n1 * 0.38 + n2 * 0.14);
        break;
      case "toxic_swamp":
        rawLift = rimT * 0.95 + n1 * 0.22 - (n2 > 0.45 ? 0.14 : 0);
        break;
      case "ancient_machine_world": {
        const tRaw = rimT * 1.55 + cornerT * 0.65 + (n1 > 0.25 ? 0.45 : 0);
        rawLift = Math.round(tRaw * 2.2) / 2.2;
        break;
      }
      default:
        rawLift = rimT * 1.1;
    }

    if (basinDepth < -0.02) {
      return basinDepth * computeFlatMask(biomeId, lx, lz, []);
    }
    return rawLift * flatMask;
  }

  function buildEnvironmentTextures(THREE, makeCanvasTex) {
    const texDuneRipple = makeCanvasTex(256, 5, 5, true, (ctx, s) => {
      ctx.fillStyle = "#dfa752";
      ctx.fillRect(0, 0, s, s);
      for (let y = 0; y < s; y += 8) {
        ctx.fillStyle = "rgba(255, 232, 184, 0.22)";
        ctx.fillRect(0, y, s, 3);
        ctx.fillStyle = "rgba(154, 96, 28, 0.22)";
        ctx.fillRect(0, y + 4, s, 2);
      }
      for (let i = 0; i < 420; i++) {
        ctx.fillStyle = i % 2 === 0 ? "rgba(255,244,214,0.16)" : "rgba(120,72,20,0.14)";
        ctx.fillRect(hash2(i, 1) * s, hash2(i, 2) * s, 3, 2);
      }
    });

    const texCrystalSand = makeCanvasTex(256, 5, 5, true, (ctx, s) => {
      ctx.fillStyle = "#e6a4cb";
      ctx.fillRect(0, 0, s, s);
      for (let y = 0; y < s; y += 10) {
        ctx.fillStyle = "rgba(253, 230, 246, 0.25)";
        ctx.fillRect(0, y, s, 3);
        ctx.fillStyle = "rgba(162, 28, 175, 0.16)";
        ctx.fillRect(0, y + 5, s, 3);
      }
      for (let i = 0; i < 120; i++) {
        ctx.fillStyle = i % 3 === 0 ? "rgba(34, 211, 238, 0.28)" : "rgba(244, 114, 182, 0.30)";
        ctx.fillRect(hash2(i, 7) * s, hash2(i, 8) * s, 4, 4);
      }
    });

    const texLavaFlow = makeCanvasTex(256, 2, 2, true, (ctx, s) => {
      ctx.fillStyle = "#7c1d13";
      ctx.fillRect(0, 0, s, s);
      for (let i = 0; i < 45; i++) {
        const g = ctx.createRadialGradient(
          hash2(i, 11) * s, hash2(i, 12) * s, 2,
          hash2(i, 11) * s, hash2(i, 12) * s, 18 + hash2(i, 13) * 28
        );
        g.addColorStop(0, "rgba(253, 224, 71, 0.85)");
        g.addColorStop(0.45, "rgba(249, 115, 22, 0.65)");
        g.addColorStop(1, "rgba(124, 29, 19, 0)");
        ctx.fillStyle = g;
        ctx.fillRect(0, 0, s, s);
      }
    });

    const texWaterfall = makeCanvasTex(128, 1, 3, true, (ctx, s) => {
      ctx.fillStyle = "#38bdf8";
      ctx.fillRect(0, 0, s, s);
      for (let i = 0; i < 90; i++) {
        ctx.fillStyle = i % 2 === 0 ? "rgba(240, 249, 255, 0.55)" : "rgba(125, 211, 252, 0.38)";
        ctx.fillRect(hash2(i, 21) * s, hash2(i, 22) * s, 3 + hash2(i, 23) * 5, 18 + hash2(i, 24) * 28);
      }
    });

    return { texDuneRipple, texCrystalSand, texLavaFlow, texWaterfall };
  }

  function buildBiomeTerrainGeometry(THREE, biome, bi, pads, palettes) {
    const segs = 44;
    const topGeo = new THREE.PlaneGeometry(31.2, 31.2, segs, segs);
    topGeo.rotateX(-Math.PI / 2);
    const pos = topGeo.attributes.position;
    const colors = new Float32Array(pos.count * 3);

    const pal = palettes[biome.id] || palettes.forest;
    const cMain = new THREE.Color(pal[0]).convertSRGBToLinear();
    const cTrail = new THREE.Color(pal[1]).convertSRGBToLinear();
    const cHigh = new THREE.Color(pal[2]).convertSRGBToLinear();
    const cBasin = new THREE.Color(pal[3] !== undefined ? pal[3] : pal[2]).convertSRGBToLinear();
    const tmpCol = new THREE.Color();

    for (let i = 0; i < pos.count; i++) {
      const lx = pos.getX(i);
      const lz = pos.getZ(i);
      const y = sampleBiomeTerrainLocalY(biome.id, lx, lz, bi, pads);
      pos.setY(i, y);

      const dCross = Math.min(Math.abs(lx), Math.abs(lz));
      const pathFactor = Math.max(0, Math.min(1, (2.2 - dCross) / 1.3));
      const n1 = Math.sin(lx * 0.42 + bi * 1.7) * Math.cos(lz * 0.38 - bi);
      const ridgeWeight = Math.max(0, Math.min(1, y * 0.55 + (n1 * 0.22 + 0.25)));

      tmpCol.copy(cMain).lerp(cHigh, ridgeWeight);
      if (y < -0.04) {
        tmpCol.lerp(cBasin, Math.min(1, Math.abs(y) * 2.6));
      }
      tmpCol.lerp(cTrail, pathFactor * 0.85);

      colors[i * 3] = tmpCol.r;
      colors[i * 3 + 1] = tmpCol.g;
      colors[i * 3 + 2] = tmpCol.b;
    }

    topGeo.setAttribute("color", new THREE.BufferAttribute(colors, 3));
    topGeo.computeVertexNormals();
    return topGeo;
  }

  function placeInstances(THREE, chunkGrp, geo, mat, transforms, colorList) {
    if (!transforms || transforms.length === 0) return null;
    const im = new THREE.InstancedMesh(geo, mat, transforms.length);
    im.castShadow = true;
    im.receiveShadow = true;
    const dummy = new THREE.Object3D();
    const c = new THREE.Color();
    for (let i = 0; i < transforms.length; i++) {
      const t = transforms[i];
      dummy.position.set(t.x, t.y, t.z);
      dummy.rotation.set(t.rx || 0, t.ry || 0, t.rz || 0);
      dummy.scale.set(t.sx || 1, t.sy || 1, t.sz || 1);
      dummy.updateMatrix();
      im.setMatrixAt(i, dummy.matrix);
      if (colorList && colorList.length > 0) {
        c.setHex(colorList[i % colorList.length]).convertSRGBToLinear();
        im.setColorAt(i, c);
      }
    }
    im.instanceMatrix.needsUpdate = true;
    if (im.instanceColor) im.instanceColor.needsUpdate = true;
    chunkGrp.add(im);
    return im;
  }

  function addPerimeterPeak(THREE, chunkGrp, x, z, radius, height, baseColor, capColor, texRock, seed) {
    const mtn = new THREE.Group();
    mtn.position.set(x, 0, z);
    const geo = new THREE.ConeGeometry(radius, height, 8, 2);
    const pos = geo.attributes.position;
    for (let i = 0; i < pos.count; i++) {
      const py = pos.getY(i);
      if (py < height * 0.4 && py > -height * 0.45) {
        pos.setX(i, pos.getX(i) * (0.85 + hash2(i, seed) * 0.34));
        pos.setZ(i, pos.getZ(i) * (0.85 + hash2(seed, i) * 0.34));
      }
    }
    geo.computeVertexNormals();
    const mat = new THREE.MeshStandardMaterial({
      color: new THREE.Color(baseColor).convertSRGBToLinear(),
      map: texRock || null,
      roughness: 0.88,
      flatShading: true
    });
    const body = new THREE.Mesh(geo, mat);
    body.position.y = height * 0.44;
    body.castShadow = true;
    body.receiveShadow = true;
    mtn.add(body);

    if (capColor !== null && capColor !== undefined) {
      const cap = new THREE.Mesh(
        new THREE.ConeGeometry(radius * 0.38, height * 0.36, 8),
        new THREE.MeshStandardMaterial({
          color: new THREE.Color(capColor).convertSRGBToLinear(),
          roughness: 0.42,
          flatShading: true
        })
      );
      cap.position.y = height * 0.78;
      mtn.add(cap);
    }
    chunkGrp.add(mtn);
    return mtn;
  }

  function addBasinFeature(THREE, chunkGrp, biome, extraTex, animList) {
    const spec = BASIN_SPECS[biome.id];
    if (!spec) return;
    const gx = biome.cx + spec.lx;
    const gz = biome.cz + spec.lz;

    const rimMat = new THREE.MeshStandardMaterial({
      color: new THREE.Color(
        spec.kind === "lava_pool" || spec.kind === "magma_lake" ? 0x291817 :
        (spec.kind === "oasis" ? 0xd99e4c :
        (spec.kind === "coolant_basin" ? 0x334155 : 0x64748b))
      ).convertSRGBToLinear(),
      roughness: 0.85
    });
    const rim = new THREE.Mesh(new THREE.TorusGeometry(1.0, 0.12, 8, 28), rimMat);
    rim.rotation.x = Math.PI / 2;
    rim.scale.set(spec.rx * 0.96, spec.rz * 0.96, 1);
    rim.position.set(gx, 0.02, gz);
    chunkGrp.add(rim);

    let waterCol = 0x22a6d4;
    let emissiveCol = 0x000000;
    let emissiveInt = 0;
    let rough = 0.14;
    let tex = null;

    if (spec.kind === "oasis" || spec.kind === "jungle_lagoon" || spec.kind === "celestial_pond") {
      waterCol = spec.kind === "oasis" ? 0x10b981 : (spec.kind === "jungle_lagoon" ? 0x14b8a6 : 0x38bdf8);
      emissiveCol = waterCol;
      emissiveInt = 0.18;
      rough = 0.1;
    } else if (spec.kind === "ice_pond" || spec.kind === "glacial_tarn") {
      waterCol = 0x99e9ff;
      emissiveCol = 0x38bdf8;
      emissiveInt = 0.16;
      rough = 0.08;
    } else if (spec.kind === "crystal_pool") {
      waterCol = 0xe879f9;
      emissiveCol = 0xc026d3;
      emissiveInt = 0.28;
      rough = 0.12;
    } else if (spec.kind === "toxic_mire") {
      waterCol = 0x84cc16;
      emissiveCol = 0x65a30d;
      emissiveInt = 0.45;
      rough = 0.25;
    } else if (spec.kind === "lava_pool" || spec.kind === "magma_lake") {
      waterCol = 0xf97316;
      emissiveCol = 0xea580c;
      emissiveInt = 0.88;
      rough = 0.35;
      tex = extraTex.texLavaFlow;
    } else if (spec.kind === "coolant_basin") {
      waterCol = 0x22d3ee;
      emissiveCol = 0x0284c7;
      emissiveInt = 0.72;
      rough = 0.18;
    }

    const disc = new THREE.Mesh(
      new THREE.CircleGeometry(1.0, 26),
      new THREE.MeshStandardMaterial({
        color: new THREE.Color(waterCol).convertSRGBToLinear(),
        map: tex,
        emissive: new THREE.Color(emissiveCol).convertSRGBToLinear(),
        emissiveIntensity: emissiveInt,
        roughness: rough,
        metalness: 0.25
      })
    );
    disc.rotation.x = -Math.PI / 2;
    disc.scale.set(spec.rx * 0.94, spec.rz * 0.94, 1);
    disc.position.set(gx, -0.14, gz);
    chunkGrp.add(disc);

    // Floating lilypads on Jungle Lagoon
    if (spec.kind === "jungle_lagoon") {
      const padM = new THREE.MeshStandardMaterial({ color: new THREE.Color(0x16a34a).convertSRGBToLinear(), roughness: 0.65 });
      const flowerM = new THREE.MeshStandardMaterial({ color: new THREE.Color(0xf472b6).convertSRGBToLinear(), emissive: new THREE.Color(0xec4899).convertSRGBToLinear(), emissiveIntensity: 0.25 });
      [[-0.9, -0.5, 0.42], [1.1, 0.4, 0.36], [0.2, -1.0, 0.32]].forEach(([ox, oz, rad], li) => {
        const pad = new THREE.Mesh(new THREE.CylinderGeometry(rad, rad, 0.05, 10), padM);
        pad.position.set(gx + ox, -0.11, gz + oz);
        chunkGrp.add(pad);
        if (li % 2 === 0) {
          const fl = new THREE.Mesh(new THREE.ConeGeometry(0.14, 0.16, 6), flowerM);
          fl.rotation.x = Math.PI;
          fl.position.set(gx + ox, -0.04, gz + oz);
          chunkGrp.add(fl);
        }
      });
    }

    // Cliff waterfall / lavafall spilling into basin for alpine, jungle, sky, volcanic, glacial biomes
    if (spec.kind === "alpine_lake" || spec.kind === "jungle_lagoon" || spec.kind === "celestial_pond" ||
        spec.kind === "lava_pool" || spec.kind === "magma_lake" || spec.kind === "glacial_tarn") {
      const wfX = gx + (spec.lx < 0 ? -spec.rx * 0.85 : spec.rx * 0.85);
      const wfZ = gz + (spec.lz < 0 ? -spec.rz * 0.72 : spec.rz * 0.72);
      const isLava = spec.kind === "lava_pool" || spec.kind === "magma_lake";
      const isFrozen = spec.kind === "glacial_tarn";

      addPerimeterPeak(
        THREE, chunkGrp, wfX, wfZ, 2.2, 4.1,
        isLava ? 0x2a1816 : (isFrozen ? 0x93c5fd : 0x566473),
        isFrozen ? 0xf0f9ff : null, null, 7.2
      );
      const fallGeo = new THREE.PlaneGeometry(1.25, 3.1);
      const fallMat = new THREE.MeshStandardMaterial({
        color: new THREE.Color(isLava ? 0xf97316 : (isFrozen ? 0xbae6fd : 0x7dd3fc)).convertSRGBToLinear(),
        map: isLava ? extraTex.texLavaFlow : extraTex.texWaterfall,
        emissive: new THREE.Color(isLava ? 0xea580c : 0x38bdf8).convertSRGBToLinear(),
        emissiveIntensity: isLava ? 0.78 : (isFrozen ? 0.2 : 0.34),
        roughness: 0.2,
        side: THREE.DoubleSide
      });
      const fallMesh = new THREE.Mesh(fallGeo, fallMat);
      fallMesh.position.set((gx + wfX) * 0.5, 1.38, (gz + wfZ) * 0.5);
      fallMesh.lookAt(gx, 0.9, gz);
      chunkGrp.add(fallMesh);

      if (!isFrozen) {
        animList.push({ type: "texScroll", map: fallMat.map, speed: isLava ? 0.35 : 0.95 });
      }
    }
  }

  // Builds bespoke monumental multi-part Landmarks for Maps 2-12 (Map 1 untouched!)
  function enhanceBiomeLandmark(THREE, biome, lmGroup, lmOrb, mats, animList) {
    if (biome.id === "forest") return 4.05; // Preserve Map 1 landmark intact

    while (lmGroup.children.length > 0) {
      lmGroup.remove(lmGroup.children[0]);
    }

    const accent = new THREE.Color(biome.accent).convertSRGBToLinear();
    const stoneMat = new THREE.MeshStandardMaterial({ color: new THREE.Color(0x526070).convertSRGBToLinear(), roughness: 0.85, flatShading: true });
    const trimMat = new THREE.MeshStandardMaterial({ color: accent, emissive: accent, emissiveIntensity: 0.32, roughness: 0.32, metalness: 0.25 });

    if (biome.id === "mountains" || biome.id === "frozen_mountains") {
      const isIce = biome.id === "frozen_mountains";
      const base = new THREE.Mesh(new THREE.CylinderGeometry(1.5, 1.85, 0.7, 8), stoneMat);
      base.position.y = 0.35;
      base.castShadow = true;
      lmGroup.add(base);

      const legMat = isIce ? trimMat : new THREE.MeshStandardMaterial({ color: new THREE.Color(0x7c4a21).convertSRGBToLinear(), roughness: 0.8 });
      for (let i = 0; i < 4; i++) {
        const a = i * Math.PI * 0.5 + Math.PI * 0.25;
        const leg = new THREE.Mesh(new THREE.CylinderGeometry(0.14, 0.2, 3.0, 7), legMat);
        leg.position.set(Math.cos(a) * 0.92, 2.0, Math.sin(a) * 0.92);
        leg.rotation.z = Math.cos(a) * 0.14;
        leg.rotation.x = -Math.sin(a) * 0.14;
        leg.castShadow = true;
        lmGroup.add(leg);
      }
      const deck = new THREE.Mesh(new THREE.CylinderGeometry(1.28, 1.28, 0.25, 8), stoneMat);
      deck.position.y = 3.35;
      lmGroup.add(deck);

      const roof = new THREE.Mesh(new THREE.ConeGeometry(1.45, 1.25, 8), trimMat);
      roof.position.y = 4.65;
      roof.castShadow = true;
      lmGroup.add(roof);

      lmOrb.position.set(0, 3.85, 0);
      lmOrb.scale.setScalar(0.5);
      lmGroup.add(lmOrb);
      return 3.85;
    } else if (biome.id === "desert" || biome.id === "jungle") {
      const isJungle = biome.id === "jungle";
      const stepCol = isJungle ? 0x3b5c49 : 0xd9a14a;
      const tierMat = new THREE.MeshStandardMaterial({ color: new THREE.Color(stepCol).convertSRGBToLinear(), roughness: 0.82, flatShading: true });
      [
        [2.9, 0.55, 0.28],
        [2.2, 0.55, 0.82],
        [1.55, 0.55, 1.36],
        [0.95, 0.55, 1.9]
      ].forEach(([w, h, py]) => {
        const tier = new THREE.Mesh(new THREE.BoxGeometry(w, h, w), tierMat);
        tier.position.y = py;
        tier.castShadow = true;
        tier.receiveShadow = true;
        lmGroup.add(tier);
      });

      const ring = new THREE.Mesh(new THREE.TorusGeometry(0.92, 0.08, 8, 24), mats.brass);
      ring.position.y = 3.35;
      lmGroup.add(ring);
      animList.push({ type: "spinY", mesh: ring, speed: 1.2 });

      lmOrb.position.set(0, 3.35, 0);
      lmOrb.scale.setScalar(0.58);
      lmGroup.add(lmOrb);
      return 3.35;
    } else if (biome.id === "frozen" || biome.id === "crystal_desert") {
      const archBase = new THREE.Mesh(new THREE.CylinderGeometry(1.6, 1.85, 0.45, 8), stoneMat);
      archBase.position.y = 0.22;
      lmGroup.add(archBase);
      const count = biome.id === "crystal_desert" ? 6 : 4;
      for (let i = 0; i < count; i++) {
        const a = (i / count) * Math.PI * 2;
        const rib = new THREE.Mesh(new THREE.ConeGeometry(0.28, 3.4, 5), trimMat);
        rib.position.set(Math.cos(a) * 1.15, 1.75, Math.sin(a) * 1.15);
        rib.rotation.z = Math.cos(a) * 0.34;
        rib.rotation.x = -Math.sin(a) * 0.34;
        rib.castShadow = true;
        lmGroup.add(rib);
      }
      const crownRing = new THREE.Mesh(new THREE.TorusGeometry(0.88, 0.09, 8, 22), trimMat);
      crownRing.rotation.x = Math.PI / 2;
      crownRing.position.y = 3.65;
      lmGroup.add(crownRing);
      animList.push({ type: "spinZ", mesh: crownRing, speed: 0.9 });

      lmOrb.position.set(0, 2.55, 0);
      lmOrb.scale.setScalar(0.62);
      lmGroup.add(lmOrb);
      return 2.55;
    } else if (biome.id === "volcanic" || biome.id === "volcanic_lands") {
      const plinth = new THREE.Mesh(new THREE.CylinderGeometry(1.65, 1.95, 0.65, 6), new THREE.MeshStandardMaterial({ color: new THREE.Color(0x271918).convertSRGBToLinear(), roughness: 0.9 }));
      plinth.position.y = 0.32;
      lmGroup.add(plinth);
      [-1.1, 1.1].forEach((hx) => {
        const horn = new THREE.Mesh(new THREE.ConeGeometry(0.42, 3.9, 6), plinth.material);
        horn.position.set(hx, 2.15, 0);
        horn.rotation.z = hx < 0 ? -0.18 : 0.18;
        horn.castShadow = true;
        lmGroup.add(horn);
      });
      const forgeRing = new THREE.Mesh(new THREE.TorusGeometry(1.05, 0.11, 8, 20), trimMat);
      forgeRing.position.y = 2.9;
      lmGroup.add(forgeRing);
      animList.push({ type: "spinY", mesh: forgeRing, speed: -1.1 });

      lmOrb.position.set(0, 2.9, 0);
      lmOrb.scale.setScalar(0.64);
      lmGroup.add(lmOrb);
      return 2.9;
    } else if (biome.id === "sky_islands") {
      const rotunda = new THREE.Mesh(new THREE.CylinderGeometry(1.1, 1.38, 2.4, 12), new THREE.MeshStandardMaterial({ color: new THREE.Color(0xe2e8f0).convertSRGBToLinear(), roughness: 0.42 }));
      rotunda.position.y = 1.2;
      rotunda.castShadow = true;
      lmGroup.add(rotunda);
      const dome = new THREE.Mesh(new THREE.SphereGeometry(1.12, 12, 8, 0, Math.PI * 2, 0, Math.PI * 0.5), mats.brass);
      dome.position.y = 2.4;
      lmGroup.add(dome);

      const vaneHub = new THREE.Group();
      vaneHub.position.set(0, 2.25, 1.18);
      for (let vi = 0; vi < 4; vi++) {
        const arm = new THREE.Mesh(new THREE.BoxGeometry(0.16, 2.1, 0.06), trimMat);
        arm.position.set(Math.cos(vi * Math.PI * 0.5) * 0.95, Math.sin(vi * Math.PI * 0.5) * 0.95, 0);
        arm.rotation.z = vi * Math.PI * 0.5 + Math.PI * 0.5;
        vaneHub.add(arm);
      }
      lmGroup.add(vaneHub);
      animList.push({ type: "spinZ", mesh: vaneHub, speed: 1.4 });

      lmOrb.position.set(0, 4.15, 0);
      lmOrb.scale.setScalar(0.5);
      lmGroup.add(lmOrb);
      return 4.15;
    } else if (biome.id === "toxic_swamp") {
      const vat = new THREE.Mesh(new THREE.CylinderGeometry(1.25, 1.05, 1.1, 10), mats.steelDark);
      vat.position.y = 0.55;
      vat.castShadow = true;
      lmGroup.add(vat);
      const stem = new THREE.Mesh(new THREE.CylinderGeometry(0.28, 0.42, 3.1, 8), new THREE.MeshStandardMaterial({ color: new THREE.Color(0x6b7280).convertSRGBToLinear(), roughness: 0.85 }));
      stem.position.y = 1.65;
      lmGroup.add(stem);
      const megaCap = new THREE.Mesh(new THREE.ConeGeometry(2.05, 1.05, 11), trimMat);
      megaCap.position.y = 3.4;
      megaCap.castShadow = true;
      lmGroup.add(megaCap);

      lmOrb.position.set(0, 4.3, 0);
      lmOrb.scale.setScalar(0.55);
      lmGroup.add(lmOrb);
      return 4.3;
    } else if (biome.id === "ancient_machine_world") {
      const plinth = new THREE.Mesh(new THREE.CylinderGeometry(1.6, 1.85, 0.6, 8), mats.steelMid);
      plinth.position.y = 0.3;
      lmGroup.add(plinth);
      const corePillar = new THREE.Mesh(new THREE.CylinderGeometry(0.45, 0.65, 3.6, 8), mats.steelDark);
      corePillar.position.y = 2.1;
      corePillar.castShadow = true;
      lmGroup.add(corePillar);

      const gyro1 = new THREE.Mesh(new THREE.TorusGeometry(1.35, 0.1, 8, 24), mats.brass);
      gyro1.position.y = 2.65;
      lmGroup.add(gyro1);
      const gyro2 = new THREE.Mesh(new THREE.TorusGeometry(1.02, 0.08, 8, 20), trimMat);
      gyro2.position.y = 2.65;
      lmGroup.add(gyro2);
      animList.push({ type: "spinY", mesh: gyro1, speed: 1.3 });
      animList.push({ type: "spinX", mesh: gyro2, speed: -1.7 });

      lmOrb.position.set(0, 4.55, 0);
      lmOrb.scale.setScalar(0.6);
      lmGroup.add(lmOrb);
      return 4.55;
    }
    return 4.05;
  }

  // Dense biome-specific InstancedMesh scatter + pathside props for Maps 2-12
  function populateBiomeScenery(THREE, chunkGrp, biome, bi, pads, deps, animList) {
    if (biome.id === "forest") return; // Preserve Map 1 untouched

    const { texRock, texFoliage, texBark, organicRockGeoA, organicRockGeoB, crystalPrismGeo, gearWheelGeo, indMats } = deps;
    const basin = BASIN_SPECS[biome.id];

    function isSafeSpot(lx, lz, minRadius) {
      if (Math.abs(lx) < 2.6 || Math.abs(lz) < 2.6) return false;
      if (Math.hypot(lx, lz) < 3.6) return false;
      if (basin) {
        const bd = Math.hypot((lx - basin.lx) / (basin.rx + 0.8), (lz - basin.lz) / (basin.rz + 0.8));
        if (bd < 1.05) return false;
      }
      for (let i = 0; i < pads.length; i++) {
        if (Math.hypot(lx - pads[i].lx, lz - pads[i].lz) < (pads[i].r + (minRadius || 0.7))) return false;
      }
      return true;
    }

    function scatterPoints(count, seedOffset, minRadius) {
      const pts = [];
      let attempts = 0;
      while (pts.length < count && attempts < count * 10) {
        attempts++;
        const rx = (hash2(attempts + bi * 31, seedOffset + 1) - 0.5) * 29.0;
        const rz = (hash2(attempts + bi * 47, seedOffset + 2) - 0.5) * 29.0;
        if (!isSafeSpot(rx, rz, minRadius)) continue;
        const ly = sampleBiomeTerrainLocalY(biome.id, rx, rz, bi, pads);
        pts.push({
          x: biome.cx + rx,
          y: ly,
          z: biome.cz + rz,
          lx: rx,
          lz: rz,
          rot: hash2(attempts, seedOffset + 3) * Math.PI * 2,
          sc: 0.72 + hash2(attempts, seedOffset + 4) * 0.65
        });
      }
      return pts;
    }

    // 1. Add corner/edge sculpted peaks & monuments framing every biome's silhouette
    const cornerOffsets = [[-14.6, -14.6], [14.6, -14.6], [-14.6, 14.6], [14.6, 14.6]];
    cornerOffsets.forEach(([ox, oz], ci) => {
      const h = 3.2 + (ci % 2) * 1.3;
      const r = 2.2 + (ci % 3) * 0.32;
      if (biome.id === "mountains" || biome.id === "frozen_mountains") {
        addPerimeterPeak(
          THREE, chunkGrp, biome.cx + ox, biome.cz + oz, r, h + 1.5,
          biome.id === "frozen_mountains" ? 0x6ba8cc : 0x5c6775, 0xf1f5f9, texRock, ci + bi
        );
      } else if (biome.id === "volcanic" || biome.id === "volcanic_lands") {
        addPerimeterPeak(
          THREE, chunkGrp, biome.cx + ox, biome.cz + oz, r, h + 1.2,
          0x291a19, 0xea580c, texRock, ci + bi + 5
        );
      } else if (biome.id === "desert" || biome.id === "crystal_desert") {
        addPerimeterPeak(
          THREE, chunkGrp, biome.cx + ox, biome.cz + oz, r * 1.1, h * 0.85,
          biome.id === "crystal_desert" ? 0xb4629a : 0xc47e35, null, texRock, ci + bi + 9
        );
      } else if (biome.id === "jungle") {
        addPerimeterPeak(
          THREE, chunkGrp, biome.cx + ox, biome.cz + oz, r * 1.05, h * 0.95,
          0x2d543e, 0x15803d, texRock, ci + bi + 13
        );
      } else if (biome.id === "frozen") {
        addPerimeterPeak(
          THREE, chunkGrp, biome.cx + ox, biome.cz + oz, r * 1.0, h * 0.9,
          0x8bc9e8, 0xf0f9ff, null, ci + bi + 17
        );
      } else if (biome.id === "toxic_swamp") {
        addPerimeterPeak(
          THREE, chunkGrp, biome.cx + ox, biome.cz + oz, r * 0.95, h * 0.72,
          0x334726, 0x84cc16, texRock, ci + bi + 21
        );
      } else if (biome.id === "ancient_machine_world") {
        const pylon = new THREE.Group();
        pylon.position.set(biome.cx + ox * 0.92, 0, biome.cz + oz * 0.92);
        const col = new THREE.Mesh(new THREE.CylinderGeometry(0.65, 0.85, 3.6, 8), indMats.steelDark);
        col.position.y = 1.8;
        col.castShadow = true;
        pylon.add(col);
        const topCog = new THREE.Mesh(gearWheelGeo, indMats.brass);
        topCog.scale.setScalar(1.8);
        topCog.position.y = 3.7;
        pylon.add(topCog);
        animList.push({ type: "spinY", mesh: topCog, speed: (ci % 2 === 0 ? 0.9 : -0.9) });
        const beacon = new THREE.Mesh(new THREE.OctahedronGeometry(0.35, 0), indMats.glowCyan);
        beacon.position.y = 4.25;
        pylon.add(beacon);
        chunkGrp.add(pylon);
      } else if (biome.id === "sky_islands") {
        const pCol = new THREE.Mesh(
          new THREE.CylinderGeometry(0.45, 0.62, 2.8, 10),
          new THREE.MeshStandardMaterial({ color: new THREE.Color(0xe2e8f0).convertSRGBToLinear(), roughness: 0.42 })
        );
        pCol.position.set(biome.cx + ox * 0.85, 1.4, biome.cz + oz * 0.85);
        pCol.castShadow = true;
        chunkGrp.add(pCol);
        const halo = new THREE.Mesh(new THREE.TorusGeometry(0.65, 0.08, 6, 16), indMats.glowCyan);
        halo.position.set(biome.cx + ox * 0.85, 3.05, biome.cz + oz * 0.85);
        halo.rotation.x = Math.PI / 2;
        chunkGrp.add(halo);
      }
    });

    // 2. Road-edge Waypoint Markers / Lanterns / Columns (gives every biome Map-1-level path framing)
    const roadMarkers = [
      [-2.85, -13.2], [2.85, -13.2], [-2.85, 13.2], [2.85, 13.2],
      [-13.2, -2.85], [-13.2, 2.85], [13.2, -2.85], [13.2, 2.85]
    ].filter(([mx, mz]) => isSafeSpot(mx, mz, 0.55));

    if (roadMarkers.length > 0) {
      const postGeo = new THREE.CylinderGeometry(0.11, 0.16, 1.45, 7);
      const glowSphereGeo = new THREE.SphereGeometry(0.18, 8, 8);
      const postCol =
        biome.id === "desert" || biome.id === "crystal_desert" ? 0xc28738 :
        biome.id === "sky_islands" ? 0xe2e8f0 :
        biome.id === "ancient_machine_world" ? 0x475569 :
        biome.id === "volcanic" || biome.id === "volcanic_lands" ? 0x291817 : 0x5c3a21;
      const glowCol =
        biome.id === "toxic_swamp" ? 0xa3e635 :
        biome.id === "crystal_desert" ? 0xf472b6 :
        biome.id === "ancient_machine_world" || biome.id === "sky_islands" || biome.id === "frozen" || biome.id === "frozen_mountains" ? 0x38bdf8 :
        0xfbbf24;

      const postMat = new THREE.MeshStandardMaterial({ color: new THREE.Color(postCol).convertSRGBToLinear(), roughness: 0.78 });
      const glowMat = new THREE.MeshStandardMaterial({
        color: new THREE.Color(glowCol).convertSRGBToLinear(),
        emissive: new THREE.Color(glowCol).convertSRGBToLinear(),
        emissiveIntensity: 0.9,
        roughness: 0.25
      });

      placeInstances(THREE, chunkGrp, postGeo, postMat, roadMarkers.map(([mx, mz]) => ({
        x: biome.cx + mx,
        y: sampleBiomeTerrainLocalY(biome.id, mx, mz, bi, pads) + 0.72,
        z: biome.cz + mz
      })));
      placeInstances(THREE, chunkGrp, glowSphereGeo, glowMat, roadMarkers.map(([mx, mz]) => ({
        x: biome.cx + mx,
        y: sampleBiomeTerrainLocalY(biome.id, mx, mz, bi, pads) + 1.56,
        z: biome.cz + mz
      })));
    }

    // 3. Universal Instanced Boulders tailored to each biome's palette
    const rockPts = scatterPoints(26, 10, 0.65);
    const rockMat = new THREE.MeshStandardMaterial({
      color: 0xffffff,
      map: texRock,
      roughness: 0.86,
      flatShading: true
    });
    const rockCols =
      biome.id === "desert" ? [0xd99b43, 0xc27b30, 0xe0b46a] :
      biome.id === "crystal_desert" ? [0xd982b5, 0xc084fc, 0xf472b6] :
      biome.id === "frozen" || biome.id === "frozen_mountains" ? [0xbae6fd, 0x93c5fd, 0xe0f2fe] :
      biome.id === "volcanic" || biome.id === "volcanic_lands" ? [0x3b2624, 0x573028, 0x261716] :
      biome.id === "toxic_swamp" ? [0x4c5e3a, 0x3f4f2f, 0x657848] :
      biome.id === "ancient_machine_world" ? [0x475569, 0x334155, 0x64748b] :
      [0x738090, 0x64748b, 0x8694a3];

    placeInstances(
      THREE, chunkGrp, organicRockGeoA, rockMat,
      rockPts.map((p) => ({
        x: p.x, y: p.y + 0.16 * p.sc, z: p.z,
        ry: p.rot, sx: 0.52 * p.sc, sy: 0.36 * p.sc, sz: 0.48 * p.sc
      })),
      rockCols
    );

    // 4. Biome-specific dense flora, structures & formations
    if (biome.id === "mountains" || biome.id === "frozen" || biome.id === "frozen_mountains") {
      const treePts = scatterPoints(24, 40, 0.85);
      const trunkGeo = new THREE.CylinderGeometry(0.11, 0.22, 1.35, 7);
      const pineGeoLow = new THREE.ConeGeometry(0.86, 1.35, 8);
      const pineGeoTop = new THREE.ConeGeometry(0.62, 1.15, 8);
      const isIcy = biome.id !== "mountains";
      const trunkMat = new THREE.MeshStandardMaterial({ color: new THREE.Color(0x6b4226).convertSRGBToLinear(), map: texBark, roughness: 0.88 });
      const pineMat = new THREE.MeshStandardMaterial({ color: 0xffffff, map: texFoliage, roughness: 0.78, flatShading: true });
      const pineCols = isIcy ? [0x67c5c9, 0x99e6ea, 0x4ea8b5] : [0x1b6e3c, 0x238a4b, 0x155930];

      placeInstances(THREE, chunkGrp, trunkGeo, trunkMat, treePts.map((p) => ({ x: p.x, y: p.y + 0.62 * p.sc, z: p.z, sx: p.sc, sy: p.sc, sz: p.sc })));
      placeInstances(THREE, chunkGrp, pineGeoLow, pineMat, treePts.map((p) => ({ x: p.x, y: p.y + 1.55 * p.sc, z: p.z, ry: p.rot, sx: p.sc, sy: p.sc, sz: p.sc })), pineCols);
      placeInstances(THREE, chunkGrp, pineGeoTop, pineMat, treePts.map((p) => ({ x: p.x, y: p.y + 2.30 * p.sc, z: p.z, ry: p.rot + 0.5, sx: p.sc, sy: p.sc, sz: p.sc })), pineCols);

      if (biome.id === "mountains") {
        // Alpine scrub & wildflowers
        const shrubPts = scatterPoints(24, 58, 0.5);
        placeInstances(THREE, chunkGrp, organicRockGeoB, pineMat, shrubPts.map((p) => ({
          x: p.x, y: p.y + 0.16 * p.sc, z: p.z, ry: p.rot, sx: 0.52 * p.sc, sy: 0.32 * p.sc, sz: 0.52 * p.sc
        })), [0x4d7c0f, 0x65a30d, 0x3f6212]);
      } else {
        const shardPts = scatterPoints(26, 65, 0.55);
        const shardMat = new THREE.MeshStandardMaterial({
          color: 0xffffff, emissive: new THREE.Color(0x06b6d4).convertSRGBToLinear(), emissiveIntensity: 0.28, roughness: 0.16, flatShading: true
        });
        placeInstances(THREE, chunkGrp, crystalPrismGeo, shardMat, shardPts.map((p) => ({
          x: p.x, y: p.y + 0.42 * p.sc, z: p.z, ry: p.rot, rz: (p.sc - 1) * 0.28, sx: 0.55 * p.sc, sy: 0.72 * p.sc, sz: 0.55 * p.sc
        })), [0xa5f3fc, 0x67e8f9, 0xbae6fd]);
      }

      // Cozy Frozen Igloo safely placed at (+11.5, +4.8) facing SW toward the ice pond
      if (biome.id === "frozen") {
        const ig = new THREE.Group();
        ig.position.set(biome.cx + 11.5, 0, biome.cz + 4.8);
        ig.rotation.y = -Math.PI * 0.65;
        const snowM = new THREE.MeshStandardMaterial({ color: new THREE.Color(0xeef6fc).convertSRGBToLinear(), roughness: 0.55 });
        const dome = new THREE.Mesh(new THREE.SphereGeometry(1.32, 14, 10, 0, Math.PI * 2, 0, Math.PI * 0.52), snowM);
        dome.castShadow = true;
        ig.add(dome);
        const entry = new THREE.Mesh(new THREE.CylinderGeometry(0.42, 0.48, 0.85, 8), snowM);
        entry.rotation.x = Math.PI / 2;
        entry.position.set(0, 0.36, 1.1);
        ig.add(entry);
        const warmGlow = new THREE.Mesh(new THREE.SphereGeometry(0.3, 8, 8), new THREE.MeshBasicMaterial({ color: 0xfbbf24 }));
        warmGlow.position.set(0, 0.34, 0.98);
        ig.add(warmGlow);
        chunkGrp.add(ig);
      }
    } else if (biome.id === "desert" || biome.id === "crystal_desert") {
      const floraPts = scatterPoints(26, 80, 0.65);
      if (biome.id === "desert") {
        const cactusMat = new THREE.MeshStandardMaterial({ color: 0xffffff, roughness: 0.72 });
        const cactusGeo = new THREE.CylinderGeometry(0.17, 0.21, 1.48, 8);
        const armGeo = new THREE.CylinderGeometry(0.10, 0.12, 0.68, 7);
        placeInstances(THREE, chunkGrp, cactusGeo, cactusMat, floraPts.map((p) => ({
          x: p.x, y: p.y + 0.70 * p.sc, z: p.z, ry: p.rot, sx: p.sc, sy: p.sc, sz: p.sc
        })), [0x2f855a, 0x38a169, 0x276749]);
        // Side Saguaro arms offset perpendicularly
        placeInstances(THREE, chunkGrp, armGeo, cactusMat, floraPts.map((p) => ({
          x: p.x + Math.cos(p.rot) * 0.30 * p.sc,
          y: p.y + 0.92 * p.sc,
          z: p.z - Math.sin(p.rot) * 0.30 * p.sc,
          ry: p.rot, rz: 0.48, sx: p.sc, sy: p.sc, sz: p.sc
        })), [0x2f855a, 0x38a169, 0x276749]);

        const agavePts = scatterPoints(24, 95, 0.55);
        placeInstances(THREE, chunkGrp, organicRockGeoB, cactusMat, agavePts.map((p) => ({
          x: p.x, y: p.y + 0.16 * p.sc, z: p.z, ry: p.rot, sx: 0.48 * p.sc, sy: 0.28 * p.sc, sz: 0.48 * p.sc
        })), [0x48bb78, 0x68d391, 0x2f855a]);

        // Oasis Palm Grove safely tucked onto NW & NE oasis edges
        if (basin) {
          [[biome.cx + basin.lx - 2.9, biome.cz + basin.lz - 1.9], [biome.cx + basin.lx + 2.8, biome.cz + basin.lz - 1.8]].forEach(([px, pz], pi) => {
            const palm = new THREE.Group();
            palm.position.set(px, 0, pz);
            const trunk = new THREE.Mesh(new THREE.CylinderGeometry(0.16, 0.25, 2.7, 8), new THREE.MeshStandardMaterial({ color: new THREE.Color(0x9c6b3b).convertSRGBToLinear(), map: texBark }));
            trunk.position.y = 1.3;
            trunk.rotation.z = pi === 0 ? 0.14 : -0.14;
            trunk.castShadow = true;
            palm.add(trunk);
            for (let fi = 0; fi < 5; fi++) {
              const a = (fi / 5) * Math.PI * 2;
              const frond = new THREE.Mesh(new THREE.ConeGeometry(0.32, 1.45, 5), new THREE.MeshStandardMaterial({ color: new THREE.Color(0x22c55e).convertSRGBToLinear(), roughness: 0.65 }));
              frond.position.set(Math.cos(a) * 0.65 + (pi === 0 ? -0.18 : 0.18), 2.55, Math.sin(a) * 0.65);
              frond.rotation.z = -Math.cos(a) * 1.15;
              frond.rotation.x = Math.sin(a) * 1.15;
              palm.add(frond);
            }
            chunkGrp.add(palm);
          });
        }
      } else {
        // Multi-shard Crystal Geode Clusters
        const prismMat = new THREE.MeshStandardMaterial({
          color: 0xffffff, emissive: new THREE.Color(0xd946ef).convertSRGBToLinear(), emissiveIntensity: 0.34, roughness: 0.18, flatShading: true
        });
        placeInstances(THREE, chunkGrp, crystalPrismGeo, prismMat, floraPts.map((p) => ({
          x: p.x, y: p.y + 0.52 * p.sc, z: p.z, ry: p.rot, rz: (p.sc - 1.0) * 0.25, sx: 0.65 * p.sc, sy: 0.94 * p.sc, sz: 0.65 * p.sc
        })), [0xf472b6, 0xe879f9, 0x22d3ee]);
        placeInstances(THREE, chunkGrp, crystalPrismGeo, prismMat, floraPts.map((p) => ({
          x: p.x + Math.cos(p.rot) * 0.28, y: p.y + 0.35 * p.sc, z: p.z + Math.sin(p.rot) * 0.28, ry: p.rot + 1.1, rx: 0.32, sx: 0.42 * p.sc, sy: 0.62 * p.sc, sz: 0.42 * p.sc
        })), [0x22d3ee, 0xf472b6, 0xc084fc]);
      }
    } else if (biome.id === "volcanic" || biome.id === "volcanic_lands") {
      const colPts = scatterPoints(34, 120, 0.6);
      const hexColGeo = new THREE.CylinderGeometry(0.34, 0.38, 1.5, 6);
      const colMat = new THREE.MeshStandardMaterial({ color: 0xffffff, roughness: 0.88, flatShading: true });
      placeInstances(THREE, chunkGrp, hexColGeo, colMat, colPts.map((p) => ({
        x: p.x, y: p.y + 0.55 * p.sc, z: p.z, ry: p.rot, sx: 0.9 * p.sc, sy: 0.6 + p.sc * 0.9, sz: 0.9 * p.sc
      })), [0x271d1c, 0x3b2422, 0x1e1615]);

      const emberPts = scatterPoints(24, 135, 0.55);
      const emberMat = new THREE.MeshStandardMaterial({
        color: 0xffffff, emissive: new THREE.Color(0xea580c).convertSRGBToLinear(), emissiveIntensity: 0.76, roughness: 0.38
      });
      placeInstances(THREE, chunkGrp, organicRockGeoB, emberMat, emberPts.map((p) => ({
        x: p.x, y: p.y + 0.14 * p.sc, z: p.z, ry: p.rot, sx: 0.36 * p.sc, sy: 0.24 * p.sc, sz: 0.36 * p.sc
      })), [0xf97316, 0xfb7185, 0xfbbf24]);
    } else if (biome.id === "jungle") {
      const fernPts = scatterPoints(36, 150, 0.55);
      const fernMat = new THREE.MeshStandardMaterial({ color: 0xffffff, map: texFoliage, roughness: 0.75 });
      placeInstances(THREE, chunkGrp, organicRockGeoB, fernMat, fernPts.map((p) => ({
        x: p.x, y: p.y + 0.24 * p.sc, z: p.z, ry: p.rot, sx: 0.68 * p.sc, sy: 0.44 * p.sc, sz: 0.68 * p.sc
      })), [0x15803d, 0x16a34a, 0x0d9488]);

      const palmPts = scatterPoints(22, 165, 0.85);
      const trunkMat = new THREE.MeshStandardMaterial({ color: new THREE.Color(0x6b4226).convertSRGBToLinear(), map: texBark });
      placeInstances(THREE, chunkGrp, new THREE.CylinderGeometry(0.14, 0.24, 2.5, 7), trunkMat, palmPts.map((p) => ({
        x: p.x, y: p.y + 1.2 * p.sc, z: p.z, sx: p.sc, sy: p.sc, sz: p.sc
      })));
      placeInstances(THREE, chunkGrp, new THREE.ConeGeometry(1.22, 0.85, 8), fernMat, palmPts.map((p) => ({
        x: p.x, y: p.y + 2.5 * p.sc, z: p.z, ry: p.rot, sx: p.sc, sy: p.sc, sz: p.sc
      })), [0x10b981, 0x059669, 0x14b8a6]);
      placeInstances(THREE, chunkGrp, new THREE.ConeGeometry(0.85, 0.65, 8), fernMat, palmPts.map((p) => ({
        x: p.x, y: p.y + 2.95 * p.sc, z: p.z, ry: p.rot + 0.6, sx: p.sc, sy: p.sc, sz: p.sc
      })), [0x34d399, 0x10b981, 0x2dd4bf]);
    } else if (biome.id === "sky_islands") {
      const skyPts = scatterPoints(24, 180, 0.75);
      const bloomMat = new THREE.MeshStandardMaterial({ color: 0xffffff, emissive: new THREE.Color(0x38bdf8).convertSRGBToLinear(), emissiveIntensity: 0.14, roughness: 0.6 });
      placeInstances(THREE, chunkGrp, new THREE.CylinderGeometry(0.11, 0.18, 1.6, 8), new THREE.MeshStandardMaterial({ color: new THREE.Color(0xcbd5e1).convertSRGBToLinear() }), skyPts.map((p) => ({
        x: p.x, y: p.y + 0.75 * p.sc, z: p.z, sx: p.sc, sy: p.sc, sz: p.sc
      })));
      placeInstances(THREE, chunkGrp, organicRockGeoB, bloomMat, skyPts.map((p) => ({
        x: p.x, y: p.y + 1.75 * p.sc, z: p.z, ry: p.rot, sx: 0.85 * p.sc, sy: 0.62 * p.sc, sz: 0.85 * p.sc
      })), [0x7dd3fc, 0xf9a8d4, 0x5eead4]);

      // 3 Satellite Floating Mini-Islands orbiting outer Sky Islands perimeter with tiny trees/gems
      [[-21, -68, 3.2], [21, -68, 2.8], [0, -89, 3.5]].forEach(([ix, iz, rad], si) => {
        const sat = new THREE.Group();
        sat.position.set(ix, 1.4 + si * 0.6, iz);
        const cap = new THREE.Mesh(new THREE.CylinderGeometry(rad, rad * 0.85, 0.65, 10), new THREE.MeshStandardMaterial({ color: new THREE.Color(0x5cc4a2).convertSRGBToLinear(), roughness: 0.7 }));
        sat.add(cap);
        const cone = new THREE.Mesh(new THREE.ConeGeometry(rad * 0.85, 2.6, 8), new THREE.MeshStandardMaterial({ color: new THREE.Color(0x94a3b8).convertSRGBToLinear(), roughness: 0.85, flatShading: true }));
        cone.rotation.x = Math.PI;
        cone.position.y = -1.6;
        sat.add(cone);
        const miniGem = new THREE.Mesh(crystalPrismGeo, indMats.glowCyan);
        miniGem.scale.setScalar(0.65);
        miniGem.position.y = 0.85;
        sat.add(miniGem);
        chunkGrp.add(sat);
        animList.push({ type: "bob", mesh: sat, baseY: sat.position.y, phase: si * 1.4, amp: 0.35 });
      });
    } else if (biome.id === "toxic_swamp") {
      const mushPts = scatterPoints(30, 210, 0.6);
      const stemMat = new THREE.MeshStandardMaterial({ color: new THREE.Color(0x6b7280).convertSRGBToLinear(), roughness: 0.85 });
      const capMat = new THREE.MeshStandardMaterial({ color: 0xffffff, emissive: new THREE.Color(0x65a30d).convertSRGBToLinear(), emissiveIntensity: 0.45, roughness: 0.4 });
      placeInstances(THREE, chunkGrp, new THREE.CylinderGeometry(0.1, 0.15, 0.65, 7), stemMat, mushPts.map((p) => ({
        x: p.x, y: p.y + 0.32 * p.sc, z: p.z, sx: p.sc, sy: p.sc, sz: p.sc
      })));
      placeInstances(THREE, chunkGrp, new THREE.ConeGeometry(0.48, 0.34, 8), capMat, mushPts.map((p) => ({
        x: p.x, y: p.y + 0.7 * p.sc, z: p.z, ry: p.rot, sx: p.sc, sy: p.sc, sz: p.sc
      })), [0x84cc16, 0xa3e635, 0x22d3ee]);

      const reedPts = scatterPoints(38, 225, 0.45);
      placeInstances(THREE, chunkGrp, new THREE.CylinderGeometry(0.04, 0.06, 1.15, 5), stemMat, reedPts.map((p) => ({
        x: p.x, y: p.y + 0.48 * p.sc, z: p.z, rx: (p.sc - 1) * 0.2, sx: 1, sy: p.sc, sz: 1
      })));
    } else if (biome.id === "ancient_machine_world") {
      const techPts = scatterPoints(26, 250, 0.75);
      placeInstances(THREE, chunkGrp, new THREE.CylinderGeometry(0.32, 0.42, 1.6, 6), indMats.steelMid, techPts.map((p) => ({
        x: p.x, y: p.y + 0.75 * p.sc, z: p.z, ry: p.rot, sx: p.sc, sy: p.sc, sz: p.sc
      })));
      placeInstances(THREE, chunkGrp, gearWheelGeo, indMats.brass, techPts.map((p) => ({
        x: p.x, y: p.y + 1.65 * p.sc, z: p.z, rx: Math.PI / 2, ry: p.rot, sx: p.sc * 0.9, sy: p.sc * 0.9, sz: p.sc * 0.9
      })));

      const relayPts = scatterPoints(22, 265, 0.65);
      const relayMat = new THREE.MeshStandardMaterial({ color: 0xffffff, emissive: new THREE.Color(0x38bdf8).convertSRGBToLinear(), emissiveIntensity: 0.75, roughness: 0.2 });
      placeInstances(THREE, chunkGrp, new THREE.OctahedronGeometry(0.28, 0), relayMat, relayPts.map((p) => ({
        x: p.x, y: p.y + 0.65 * p.sc, z: p.z, ry: p.rot, sx: p.sc, sy: p.sc * 1.4, sz: p.sc
      })), [0x38bdf8, 0x22d3ee, 0xa855f7]);
    }
  }

  window.BiomeEnvBuilder = {
    BIOME_ATMOSPHERE,
    buildEnvironmentTextures,
    setBiomePads,
    sampleBiomeTerrainLocalY,
    buildBiomeTerrainGeometry,
    addBasinFeature,
    enhanceBiomeLandmark,
    populateBiomeScenery,
    updateAnimations: function (now, dt, visibleChunks) {
      const scrolledTex = new Set();
      const keys = Object.keys(animatedPropsByBiome);
      for (let ki = 0; ki < keys.length; ki++) {
        const bId = keys[ki];
        const cg = visibleChunks[bId];
        if (cg && !cg.visible) continue;
        const list = animatedPropsByBiome[bId];
        for (let i = 0; i < list.length; i++) {
          const item = list[i];
          if (item.type === "spinY") item.mesh.rotation.y += dt * item.speed;
          else if (item.type === "spinX") item.mesh.rotation.x += dt * item.speed;
          else if (item.type === "spinZ") item.mesh.rotation.z += dt * item.speed;
          else if (item.type === "bob") item.mesh.position.y = item.baseY + Math.sin(now * 0.0014 + item.phase) * item.amp;
          else if (item.type === "texScroll" && item.map && !scrolledTex.has(item.map)) {
            scrolledTex.add(item.map);
            item.map.offset.y = (item.map.offset.y - dt * item.speed) % 1;
          }
        }
      }
    },
    getAnimList: function (biomeId) {
      if (!animatedPropsByBiome[biomeId]) animatedPropsByBiome[biomeId] = [];
      return animatedPropsByBiome[biomeId];
    }
  };
})();
