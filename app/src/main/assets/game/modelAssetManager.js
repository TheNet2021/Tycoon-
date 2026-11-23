(function () {
  "use strict";

  // =========================================================================
  // KENNEY GLTF / GLB ASSET MANAGER (MAPS 2-12 ONLY — MAP 1 PRESERVED)
  // Lazy-loads only assets needed by active/visible biome chunks, caches
  // geometries/materials, deduplicates shared colormap textures, builds
  // Three.js LOD hierarchies for buildings/machines, and supplies batched
  // InstancedMesh geometry parts for Kenney Mini Forest flora & rocks.
  // =========================================================================

  const IND = "models/kenney-city-kit-industrial/";
  const FOR = "models/kenney-mini-forest/";
  const HEX = "models/kenney-hexagon-kit/";

  // Strictly curated per-biome model manifests (zero Map 1 assets, zero excluded archer/weapon/target assets)
  const BIOME_ASSET_MANIFEST = {
    forest: [], // Map 1 strictly untouched
    mountains: [
      IND + "building-i.glb", IND + "chimney-large.glb",
      IND + "building-d.glb", IND + "detail-tank.glb",
      IND + "building-b.glb", IND + "chimney-basic.glb",
      IND + "shipping-container-a.glb",
      FOR + "tree-high.glb", FOR + "tree.glb", FOR + "plant.glb",
      FOR + "rocks-high.glb", FOR + "rocks-low.glb", FOR + "stones.glb",
      FOR + "tent.glb", FOR + "fence.glb", FOR + "flag.glb",
      // Hexagon Settlement Sanctuary (coherent district West of Mountains)
      HEX + "building-cabin.glb", HEX + "building-house.glb", HEX + "building-village.glb",
      HEX + "building-farm.glb", HEX + "building-market.glb", HEX + "building-mine.glb",
      HEX + "building-smelter.glb", HEX + "building-mill.glb", HEX + "building-watermill.glb",
      HEX + "grass.glb", HEX + "grass-forest.glb", HEX + "grass-hill.glb",
      HEX + "dirt.glb", HEX + "dirt-lumber.glb",
      HEX + "stone.glb", HEX + "stone-hill.glb", HEX + "stone-mountain.glb",
      HEX + "water.glb", HEX + "water-island.glb", HEX + "bridge.glb",
      HEX + "path-straight.glb", HEX + "path-corner.glb", HEX + "path-corner-sharp.glb",
      HEX + "path-crossing.glb", HEX + "path-end.glb", HEX + "path-start.glb",
      HEX + "path-square.glb", HEX + "path-square-end.glb",
      HEX + "path-intersectionB.glb", HEX + "path-intersectionC.glb",
      HEX + "river-straight.glb", HEX + "river-corner.glb", HEX + "river-corner-sharp.glb",
      HEX + "river-start.glb", HEX + "river-end.glb", HEX + "river-crossing.glb",
      HEX + "river-intersectionA.glb", HEX + "river-intersectionB.glb"
    ],
    desert: [
      IND + "building-e.glb", IND + "solar-panel-landscape-group.glb",
      IND + "building-c.glb", IND + "detail-tank.glb", IND + "solar-panel-flat.glb",
      IND + "chimney-small.glb",
      FOR + "rocks-low.glb", FOR + "stones.glb", FOR + "tent.glb", FOR + "flag.glb"
    ],
    frozen: [
      IND + "building-k.glb", IND + "detail-tank-large.glb",
      IND + "building-h.glb",
      FOR + "tree-high.glb", FOR + "tree.glb",
      FOR + "rocks-high.glb", FOR + "rocks-low.glb", FOR + "stones.glb",
      FOR + "tent.glb", FOR + "flag.glb"
    ],
    volcanic: [
      IND + "building-m.glb", IND + "chimney-medium.glb",
      IND + "building-p.glb", IND + "chimney-large.glb", IND + "shipping-container-b.glb",
      FOR + "rocks-high.glb", FOR + "rocks-low.glb", FOR + "stones.glb"
    ],
    jungle: [
      // Pure natural/expedition + coherent riverside Hexagon outpost (zero industrial steel/concrete factories)
      FOR + "building-platform.glb", FOR + "building-structure.glb", FOR + "building-roof.glb",
      FOR + "tent.glb", FOR + "fence.glb", FOR + "flag.glb",
      FOR + "tree-high.glb", FOR + "tree.glb", FOR + "plant.glb",
      FOR + "rocks-low.glb", FOR + "stones.glb",
      HEX + "grass.glb", HEX + "grass-forest.glb", HEX + "dirt-lumber.glb",
      HEX + "water.glb", HEX + "water-island.glb", HEX + "bridge.glb",
      HEX + "river-start.glb", HEX + "river-straight.glb", HEX + "river-corner.glb", HEX + "river-corner-sharp.glb", HEX + "river-end.glb",
      HEX + "path-start.glb", HEX + "path-straight.glb", HEX + "path-end.glb",
      HEX + "building-cabin.glb", HEX + "building-farm.glb", HEX + "building-watermill.glb"
    ],
    crystal_desert: [
      IND + "building-g.glb", IND + "solar-panel-portrait-group.glb",
      IND + "building-j.glb", IND + "solar-panel-portrait.glb",
      FOR + "rocks-low.glb", FOR + "stones.glb",
      HEX + "sand.glb", HEX + "sand-desert.glb", HEX + "water.glb", HEX + "water-island.glb",
      HEX + "path-start.glb", HEX + "path-straight.glb", HEX + "path-corner.glb", HEX + "path-square.glb", HEX + "path-end.glb",
      HEX + "building-market.glb", HEX + "building-village.glb"
    ],
    sky_islands: [
      IND + "windmill.glb", IND + "windmill-low.glb",
      FOR + "plant.glb", FOR + "stones.glb", FOR + "flag.glb",
      HEX + "grass.glb", HEX + "grass-hill.glb", HEX + "stone-hill.glb", HEX + "water-island.glb",
      HEX + "path-start.glb", HEX + "path-straight.glb", HEX + "path-square.glb", HEX + "path-end.glb",
      HEX + "building-mill.glb", HEX + "building-house.glb"
    ],
    toxic_swamp: [
      IND + "building-f.glb", IND + "water-tower.glb", IND + "detail-tank-large.glb",
      FOR + "plant.glb", FOR + "rocks-low.glb", FOR + "stones.glb", FOR + "fence.glb"
    ],
    frozen_mountains: [
      IND + "building-l.glb", IND + "chimney-small.glb",
      FOR + "tree-high.glb", FOR + "tree.glb",
      FOR + "rocks-high.glb", FOR + "rocks-low.glb", FOR + "stones.glb",
      HEX + "stone.glb", HEX + "stone-hill.glb", HEX + "stone-mountain.glb", HEX + "dirt.glb",
      HEX + "path-start.glb", HEX + "path-straight.glb", HEX + "path-end.glb",
      HEX + "building-mine.glb", HEX + "building-smelter.glb"
    ],
    volcanic_lands: [
      IND + "building-n.glb", IND + "chimney-large.glb",
      FOR + "rocks-high.glb", FOR + "rocks-low.glb", FOR + "stones.glb"
    ],
    ancient_machine_world: [
      IND + "building-s.glb", IND + "detail-tank-large.glb",
      IND + "building-t.glb", IND + "shipping-container-a.glb"
    ]
  };

  // Building GLB assemblies mapped 1:1 to BUILDING_DEFS (Maps 2-12)
  const BUILDING_ASSEMBLIES = {
    smelter: {
      main: { url: IND + "building-i.glb", footprint: 2.55, maxH: 3.1, x: -0.15, z: 0 },
      acc: [
        { url: IND + "chimney-large.glb", footprint: 0.78, maxH: 3.1, x: 0.88, z: -0.78 },
        { url: IND + "shipping-container-a.glb", footprint: 1.1, maxH: 1.0, x: 0.94, z: 0.65, ry: Math.PI * 0.5 }
      ]
    },
    miner_mountains: {
      main: { url: IND + "building-d.glb", footprint: 2.4, maxH: 3.0, x: 0.12, z: -0.08 },
      acc: [
        { url: IND + "detail-tank.glb", footprint: 0.88, maxH: 1.5, x: -0.95, z: 0.52 }
      ]
    },
    miner_desert: {
      main: { url: IND + "building-e.glb", footprint: 2.35, maxH: 2.95, x: 0.15, z: -0.2 },
      acc: [
        { url: IND + "solar-panel-landscape-group.glb", footprint: 1.48, maxH: 1.1, x: -0.65, z: 0.78 }
      ]
    },
    miner_frozen: {
      main: { url: IND + "building-k.glb", footprint: 2.55, maxH: 3.0, x: 0.1, z: -0.05 },
      acc: [
        { url: IND + "detail-tank-large.glb", footprint: 1.02, maxH: 1.9, x: -0.96, z: 0.52 }
      ]
    },
    miner_volcanic: {
      main: { url: IND + "building-m.glb", footprint: 2.4, maxH: 3.1, x: -0.12, z: 0 },
      acc: [
        { url: IND + "chimney-medium.glb", footprint: 0.75, maxH: 2.8, x: 0.88, z: -0.75 }
      ]
    },
    miner_jungle: {
      // Pure Kenney Mini Forest timber lodge + expedition tent (zero industrial metal)
      main: { url: FOR + "building-platform.glb", footprint: 2.35, maxH: 0.9, x: 0, z: 0 },
      acc: [
        { url: FOR + "building-structure.glb", footprint: 1.75, maxH: 1.75, x: 0, z: -0.02, y: 0.88 },
        { url: FOR + "building-roof.glb", footprint: 2.08, maxH: 1.65, x: 0, z: -0.02, y: 2.60 },
        { url: FOR + "tent.glb", footprint: 0.98, maxH: 1.1, x: -0.95, z: 0.72, ry: 0.45 }
      ]
    },
    miner_crystal_desert: {
      main: { url: IND + "building-g.glb", footprint: 2.38, maxH: 2.95, x: 0.15, z: -0.12 },
      acc: [
        { url: IND + "solar-panel-portrait-group.glb", footprint: 1.38, maxH: 1.2, x: -0.75, z: 0.78 }
      ]
    },
    miner_sky_islands: {
      main: { url: IND + "windmill.glb", footprint: 2.05, maxH: 3.9, x: -0.22, z: -0.1 },
      acc: [
        { url: IND + "windmill-low.glb", footprint: 1.3, maxH: 2.4, x: 0.88, z: 0.42, ry: -0.4 }
      ]
    },
    miner_toxic_swamp: {
      main: { url: IND + "building-f.glb", footprint: 2.25, maxH: 2.75, x: -0.15, z: 0.12 },
      acc: [
        { url: IND + "water-tower.glb", footprint: 1.05, maxH: 3.1, x: 0.88, z: -0.72 },
        { url: IND + "detail-tank-large.glb", footprint: 0.9, maxH: 1.7, x: -0.92, z: -0.72 }
      ]
    },
    miner_frozen_mountains: {
      main: { url: IND + "building-l.glb", footprint: 2.42, maxH: 3.1, x: -0.1, z: 0 },
      acc: [
        { url: IND + "chimney-small.glb", footprint: 0.68, maxH: 2.5, x: 0.88, z: -0.76 }
      ]
    },
    miner_volcanic_lands: {
      main: { url: IND + "building-n.glb", footprint: 2.45, maxH: 3.2, x: -0.1, z: 0 },
      acc: [
        { url: IND + "chimney-large.glb", footprint: 0.8, maxH: 3.0, x: 0.88, z: -0.78 }
      ]
    },
    miner_ancient_machine: {
      main: { url: IND + "building-s.glb", footprint: 2.6, maxH: 3.2, x: 0.12, z: 0 },
      acc: [
        { url: IND + "detail-tank-large.glb", footprint: 0.98, maxH: 1.9, x: -0.98, z: -0.68 }
      ]
    }
  };

  // Industrial Machine Housing Assemblies mapped to compatible Maps 2-12 MACHINE_DEFS
  // Preserves animated rotors, arms, molten crucibles, gimbals, and smoke stacks!
  const MACHINE_ASSEMBLIES = {
    smelting_furnace: {
      main: { url: IND + "building-b.glb", footprint: 2.35, maxH: 2.35, x: 0, z: -0.25 },
      acc: [
        { url: IND + "chimney-basic.glb", footprint: 0.68, maxH: 3.0, x: -0.68, z: -0.78 },
        { url: IND + "shipping-container-a.glb", footprint: 1.0, maxH: 0.95, x: 0.95, z: 0.68, ry: Math.PI * 0.5 }
      ]
    },
    gold_refinery: {
      main: { url: IND + "building-c.glb", footprint: 2.35, maxH: 2.4, x: -0.05, z: -0.22 },
      acc: [
        { url: IND + "chimney-small.glb", footprint: 0.62, maxH: 2.6, x: -0.68, z: -0.78 },
        { url: IND + "detail-tank.glb", footprint: 0.78, maxH: 1.4, x: 1.02, z: 0.25 },
        { url: IND + "solar-panel-flat.glb", footprint: 0.9, maxH: 0.6, x: -0.92, z: 0.75 }
      ]
    },
    crystal_processor: {
      main: { url: IND + "building-h.glb", footprint: 2.3, maxH: 2.4, x: 0, z: -0.18 },
      acc: [
        { url: IND + "detail-tank-large.glb", footprint: 0.88, maxH: 1.75, x: -1.02, z: 0.28 }
      ]
    },
    industrial_factory: {
      main: { url: IND + "building-p.glb", footprint: 2.45, maxH: 2.5, x: 0, z: -0.18 },
      acc: [
        { url: IND + "chimney-large.glb", footprint: 0.72, maxH: 2.85, x: 0.92, z: -0.82 },
        { url: IND + "chimney-medium.glb", footprint: 0.62, maxH: 2.45, x: -0.92, z: -0.82 },
        { url: IND + "shipping-container-b.glb", footprint: 1.05, maxH: 0.95, x: 0.95, z: 0.68, ry: Math.PI * 0.5 }
      ]
    },
    crystal_synthesizer: {
      main: { url: IND + "building-j.glb", footprint: 2.3, maxH: 2.4, x: 0, z: -0.18 },
      acc: [
        { url: IND + "solar-panel-portrait.glb", footprint: 0.88, maxH: 1.0, x: -0.95, z: 0.72 }
      ]
    },
    titan_forge: {
      main: { url: IND + "building-t.glb", footprint: 2.4, maxH: 2.55, x: 0, z: -0.18 },
      acc: [
        { url: IND + "shipping-container-a.glb", footprint: 1.05, maxH: 0.95, x: -0.95, z: 0.72 }
      ]
    }
  };

  let loaderInstance = null;
  const assetCache = new Map();       // url -> Promise<AssetRecord | null>
  const textureCache = new Map();     // cacheKey -> THREE.Texture
  const materialCache = new Map();    // cacheKey -> THREE.MeshStandardMaterial
  const biomeState = {};              // biomeId -> "idle" | "loading" | "ready"
  const pendingBuildingMounts = [];   // mounts awaiting biome load
  const pendingMachineMounts = [];
  const sceneryHooksByBiome = {};     // biomeId -> Array<fn(manager, THREE)>

  function getLoader(THREE) {
    if (!loaderInstance && THREE && typeof THREE.GLTFLoader === "function") {
      if (THREE.Cache) THREE.Cache.enabled = true;
      loaderInstance = new THREE.GLTFLoader();
    }
    return loaderInstance;
  }

  function normalizeMaterial(THREE, mat, packKey) {
    if (!mat) return mat;
    const matName = mat.name || "colormap";
    const key = packKey + "::" + matName;
    if (materialCache.has(key)) return materialCache.get(key);

    const out = mat.clone();
    if (out.map) {
      const texKey = packKey + "::" + (out.map.name || "colormap");
      if (textureCache.has(texKey)) {
        if (out.map !== textureCache.get(texKey)) out.map.dispose();
        out.map = textureCache.get(texKey);
      } else {
        out.map.encoding = THREE.sRGBEncoding;
        out.map.anisotropy = 2;
        out.map.needsUpdate = true;
        textureCache.set(texKey, out.map);
      }
    }
    out.roughness = typeof out.roughness === "number" ? Math.max(0.55, out.roughness) : 0.72;
    out.metalness = typeof out.metalness === "number" ? Math.min(0.25, out.metalness) : 0.05;
    out.needsUpdate = true;
    materialCache.set(key, out);
    return out;
  }

  function loadModel(THREE, url) {
    if (!url) return Promise.resolve(null);
    if (assetCache.has(url)) return assetCache.get(url);

    const loader = getLoader(THREE);
    if (!loader) {
      return Promise.resolve(null);
    }

    const packKey = url.indexOf("kenney-mini-forest") !== -1
      ? "forest_kit"
      : (url.indexOf("kenney-hexagon-kit") !== -1 ? "hexagon_kit" : "industrial_kit");

    const p = new Promise((resolve) => {
      loader.load(
        url,
        (gltf) => {
          const root = gltf.scene || (gltf.scenes && gltf.scenes[0]);
          if (!root) {
            resolve(null);
            return;
          }
          root.updateMatrixWorld(true);
          const box = new THREE.Box3().setFromObject(root);
          const size = new THREE.Vector3();
          const center = new THREE.Vector3();
          box.getSize(size);
          box.getCenter(center);

          const parts = [];
          root.traverse((obj) => {
            if (obj.isMesh) {
              obj.castShadow = true;
              obj.receiveShadow = true;
              obj.material = Array.isArray(obj.material)
                ? obj.material.map((m) => normalizeMaterial(THREE, m, packKey))
                : normalizeMaterial(THREE, obj.material, packKey);
              parts.push({
                geometry: obj.geometry,
                material: obj.material,
                matrixWorld: obj.matrixWorld.clone()
              });
            }
          });

          resolve({ url, root, box, size, center, minY: box.min.y, parts });
        },
        undefined,
        (err) => {
          if (typeof console !== "undefined" && console.warn) {
            console.warn("[ModelAssetManager] Failed to load", url, err);
          }
          resolve(null);
        }
      );
    });

    assetCache.set(url, p);
    return p;
  }

  function computeFittedHeight(asset, spec) {
    if (!asset) return 1.5;
    const sx = Math.max(asset.size.x, 0.01);
    const sy = Math.max(asset.size.y, 0.01);
    const sz = Math.max(asset.size.z, 0.01);
    const maxXZ = Math.max(sx, sz);
    const scaleXZ = (spec.footprint || 2.0) / maxXZ;
    const scaleY = spec.maxH ? spec.maxH / sy : scaleXZ;
    const uniformScale = Math.min(scaleXZ, scaleY);
    return (spec.y || 0) + sy * uniformScale;
  }

  function cloneFitted(THREE, asset, spec) {
    if (!asset || !asset.root) return null;
    const holder = new THREE.Group();
    const clone = asset.root.clone(true);

    const sx = Math.max(asset.size.x, 0.01);
    const sy = Math.max(asset.size.y, 0.01);
    const sz = Math.max(asset.size.z, 0.01);
    const maxXZ = Math.max(sx, sz);

    const scaleXZ = (spec.footprint || 2.0) / maxXZ;
    const scaleY = spec.maxH ? spec.maxH / sy : scaleXZ;
    const uniformScale = Math.min(scaleXZ, scaleY);

    clone.scale.setScalar(uniformScale);
    clone.position.set(
      -asset.center.x * uniformScale,
      -asset.minY * uniformScale,
      -asset.center.z * uniformScale
    );
    holder.add(clone);
    holder.position.set(spec.x || 0, spec.y || 0, spec.z || 0);
    holder.rotation.y = spec.ry || 0;
    return holder;
  }

  function createAssemblyLOD(THREE, mainAsset, accAssets, spec) {
    const lod = new THREE.LOD();

    // LOD 0 (near < 48u): Main building + chimneys/tanks/solar/containers
    const nearGrp = new THREE.Group();
    const mainNear = cloneFitted(THREE, mainAsset, spec.main);
    if (mainNear) nearGrp.add(mainNear);
    let mainTopY = computeFittedHeight(mainAsset, spec.main);
    let maxTopY = mainTopY;
    if (spec.acc) {
      for (let i = 0; i < spec.acc.length; i++) {
        const sub = cloneFitted(THREE, accAssets[i], spec.acc[i]);
        if (sub) {
          nearGrp.add(sub);
          const subTop = computeFittedHeight(accAssets[i], spec.acc[i]);
          if (subTop > maxTopY) maxTopY = subTop;
          if (spec.acc[i].url && spec.acc[i].url.indexOf("building-roof") !== -1) {
            mainTopY = Math.max(mainTopY, subTop);
          }
        }
      }
    }
    lod.addLevel(nearGrp, 0);

    // LOD 1 (far >= 48u): Primary structure only for lower draw/triangle count
    const farGrp = new THREE.Group();
    const mainFar = cloneFitted(THREE, mainAsset, spec.main);
    if (mainFar) farGrp.add(mainFar);
    lod.addLevel(farGrp, 48);

    return { lod, mainTopY, maxTopY };
  }

  function disposeGroupChildren(grp) {
    while (grp.children.length > 0) {
      const ch = grp.children[0];
      grp.remove(ch);
      ch.traverse((obj) => {
        if (obj.isMesh && obj.userData && obj.userData.isPlaceholder && obj.geometry) {
          obj.geometry.dispose();
        }
      });
    }
  }

  function removePlaceholderMeshes(chunkGrp, oldMeshes) {
    if (!oldMeshes || oldMeshes.length === 0) return;
    for (let i = 0; i < oldMeshes.length; i++) {
      const m = oldMeshes[i];
      if (!m) continue;
      if (m.parent) m.parent.remove(m);
      else if (chunkGrp) chunkGrp.remove(m);
      if (typeof m.dispose === "function") m.dispose();
    }
  }

  // Extract normalized InstancedMesh(es) for a Mini Forest GLB model onto chunkGrp
  function buildInstancedFromAsset(THREE, chunkGrp, asset, transforms, scaleMult, tintHexList) {
    if (!asset || !asset.parts || asset.parts.length === 0 || !transforms || transforms.length === 0) return [];
    const created = [];
    const maxXZ = Math.max(asset.size.x, asset.size.z, 0.01);
    const normScale = (scaleMult || 1.0) / maxXZ;

    const normMatrix = new THREE.Matrix4().makeTranslation(
      -asset.center.x,
      -asset.minY,
      -asset.center.z
    );
    const scaleMat = new THREE.Matrix4().makeScale(normScale, normScale, normScale);
    normMatrix.premultiply(scaleMat);

    const dummy = new THREE.Object3D();
    const partMatrix = new THREE.Matrix4();
    const instanceCol = new THREE.Color();

    for (let pi = 0; pi < asset.parts.length; pi++) {
      const part = asset.parts[pi];
      const mat = Array.isArray(part.material) ? part.material[0] : part.material;
      const im = new THREE.InstancedMesh(part.geometry, mat, transforms.length);
      im.castShadow = true;
      im.receiveShadow = true;
      im.frustumCulled = true;

      const localCombined = new THREE.Matrix4().multiplyMatrices(normMatrix, part.matrixWorld);

      for (let ti = 0; ti < transforms.length; ti++) {
        const t = transforms[ti];
        dummy.position.set(t.x, t.y, t.z);
        dummy.rotation.set(t.rx || 0, t.ry || 0, t.rz || 0);
        dummy.scale.set(t.sx || 1, t.sy || 1, t.sz || 1);
        dummy.updateMatrix();
        partMatrix.multiplyMatrices(dummy.matrix, localCombined);
        im.setMatrixAt(ti, partMatrix);
        if (tintHexList && tintHexList.length > 0) {
          instanceCol.setHex(tintHexList[ti % tintHexList.length]).convertSRGBToLinear();
          im.setColorAt(ti, instanceCol);
        }
      }
      im.instanceMatrix.needsUpdate = true;
      if (im.instanceColor) im.instanceColor.needsUpdate = true;
      chunkGrp.add(im);
      created.push(im);
    }
    return created;
  }

  // Clone a Hexagon Kit tile/building preserving authentic hex plinth coordinate origin (y = 0) and extracting animated rotors
  function cloneHexTile(THREE, asset, hexScale, x, y, z, ry) {
    if (!asset || !asset.root) return null;
    const holder = new THREE.Group();
    const clone = asset.root.clone(true);
    clone.scale.setScalar(hexScale || 1.0);
    holder.add(clone);
    holder.position.set(x || 0, y || 0, z || 0);
    holder.rotation.y = ry || 0;

    const rotors = [];
    clone.traverse((obj) => {
      if (obj.name && obj.name.indexOf("rotate-") === 0) {
        rotors.push({ mesh: obj, axis: obj.name.slice(7) });
      }
    });
    holder.userData.animRotors = rotors;
    return holder;
  }

  // Batch Hexagon Kit static tiles into shared InstancedMeshes with exact hex plinth origin
  function buildInstancedHexTiles(THREE, chunkGrp, asset, transforms, hexScale) {
    if (!asset || !asset.parts || asset.parts.length === 0 || !transforms || transforms.length === 0) return [];
    const created = [];
    const s = hexScale || 1.0;
    const scaleMat = new THREE.Matrix4().makeScale(s, s, s);
    const dummy = new THREE.Object3D();
    const partMatrix = new THREE.Matrix4();

    for (let pi = 0; pi < asset.parts.length; pi++) {
      const part = asset.parts[pi];
      const mat = Array.isArray(part.material) ? part.material[0] : part.material;
      const im = new THREE.InstancedMesh(part.geometry, mat, transforms.length);
      im.castShadow = true;
      im.receiveShadow = true;
      im.frustumCulled = true;

      const localCombined = new THREE.Matrix4().multiplyMatrices(scaleMat, part.matrixWorld);
      for (let ti = 0; ti < transforms.length; ti++) {
        const t = transforms[ti];
        dummy.position.set(t.x, t.y || 0, t.z);
        dummy.rotation.set(0, t.ry || 0, 0);
        dummy.scale.setScalar(t.s || 1);
        dummy.updateMatrix();
        partMatrix.multiplyMatrices(dummy.matrix, localCombined);
        im.setMatrixAt(ti, partMatrix);
      }
      im.instanceMatrix.needsUpdate = true;
      chunkGrp.add(im);
      created.push(im);
    }
    return created;
  }

  function registerBuildingMount(def, structGroup, legacyBodyGroup, mountY, onMounted) {
    if (def.biome === "forest") return; // Preserve Map 1 untouched
    const spec = BUILDING_ASSEMBLIES[def.id];
    if (!spec) return;
    pendingBuildingMounts.push({
      id: def.id,
      biome: def.biome,
      spec,
      structGroup,
      legacyBodyGroup,
      mountY: mountY || 0.16,
      onMounted: onMounted || null,
      mounted: false
    });
  }

  function registerMachineMount(def, rigGroup, legacyBodyGroup, mountY, onMounted) {
    if (def.biome === "forest") return; // Preserve Map 1 untouched
    const spec = MACHINE_ASSEMBLIES[def.id];
    if (!spec) return;
    pendingMachineMounts.push({
      id: def.id,
      biome: def.biome,
      spec,
      rigGroup,
      legacyBodyGroup,
      mountY: mountY || 0.16,
      onMounted: onMounted || null,
      mounted: false
    });
  }

  function registerBiomeSceneryHook(biomeId, hookFn) {
    if (biomeId === "forest" || typeof hookFn !== "function") return;
    if (biomeState[biomeId] === "ready") {
      hookFn(window.ModelAssetManager, window.THREE);
      return;
    }
    if (!sceneryHooksByBiome[biomeId]) sceneryHooksByBiome[biomeId] = [];
    sceneryHooksByBiome[biomeId].push(hookFn);
  }

  function mountPendingForBiome(THREE, biomeId) {
    const loader = getLoader(THREE);
    if (!loader) return;

    // 1. Upgrade matching buildings in this biome to LOD GLB complexes
    for (let i = 0; i < pendingBuildingMounts.length; i++) {
      const m = pendingBuildingMounts[i];
      if (m.biome !== biomeId || m.mounted) continue;
      m.mounted = true;
      const allUrls = [m.spec.main.url].concat((m.spec.acc || []).map((a) => a.url));
      Promise.all(allUrls.map((u) => loadModel(THREE, u))).then((assets) => {
        const mainAsset = assets[0];
        if (!mainAsset) return;
        const built = createAssemblyLOD(THREE, mainAsset, assets.slice(1), m.spec);
        built.lod.position.y = m.mountY;
        if (m.legacyBodyGroup) {
          m.legacyBodyGroup.visible = false;
          disposeGroupChildren(m.legacyBodyGroup);
        }
        m.structGroup.add(built.lod);
        if (typeof m.onMounted === "function") {
          m.onMounted({
            mainTopY: m.mountY + built.mainTopY,
            maxTopY: m.mountY + built.maxTopY,
            mainX: m.spec.main.x || 0,
            mainZ: m.spec.main.z || 0
          });
        }
      });
    }

    // 2. Upgrade compatible industrial machine static bases in Maps 2-12 while keeping rotors/arms/LEDs alive
    for (let i = 0; i < pendingMachineMounts.length; i++) {
      const m = pendingMachineMounts[i];
      if (m.biome !== biomeId || m.mounted) continue;
      m.mounted = true;
      const allUrls = [m.spec.main.url].concat((m.spec.acc || []).map((a) => a.url));
      Promise.all(allUrls.map((u) => loadModel(THREE, u))).then((assets) => {
        const mainAsset = assets[0];
        if (!mainAsset) return;
        const built = createAssemblyLOD(THREE, mainAsset, assets.slice(1), m.spec);
        built.lod.position.y = m.mountY;
        if (m.legacyBodyGroup) {
          m.legacyBodyGroup.visible = false;
          disposeGroupChildren(m.legacyBodyGroup);
        }
        m.rigGroup.add(built.lod);
        if (typeof m.onMounted === "function") {
          m.onMounted({
            mainTopY: m.mountY + built.mainTopY,
            maxTopY: m.mountY + built.maxTopY,
            mainX: m.spec.main.x || 0,
            mainZ: m.spec.main.z || 0
          });
        }
      });
    }

    // 3. Upgrade biome scenery / expedition camps & Hexagon districts
    const hooks = sceneryHooksByBiome[biomeId];
    if (hooks && hooks.length > 0) {
      delete sceneryHooksByBiome[biomeId];
      for (let hi = 0; hi < hooks.length; hi++) {
        hooks[hi](window.ModelAssetManager, THREE);
      }
    }
  }

  function ensureBiomeAssets(THREE, biomeId) {
    if (!biomeId || biomeId === "forest") return; // Map 1 strictly untouched
    if (biomeState[biomeId] === "loading" || biomeState[biomeId] === "ready") return;
    if (!getLoader(THREE)) return;

    const urls = BIOME_ASSET_MANIFEST[biomeId] || [];
    if (urls.length === 0) {
      biomeState[biomeId] = "ready";
      mountPendingForBiome(THREE, biomeId);
      return;
    }

    biomeState[biomeId] = "loading";
    Promise.all(urls.map((u) => loadModel(THREE, u))).then(() => {
      biomeState[biomeId] = "ready";
      mountPendingForBiome(THREE, biomeId);
    });
  }

  window.ModelAssetManager = {
    IND,
    FOR,
    HEX,
    BIOME_ASSET_MANIFEST,
    loadModel: function (THREE, url) {
      return loadModel(THREE, url);
    },
    cloneFitted: function (THREE, asset, spec) {
      return cloneFitted(THREE, asset, spec);
    },
    cloneHexTile: function (THREE, asset, hexScale, x, y, z, ry) {
      return cloneHexTile(THREE, asset, hexScale, x, y, z, ry);
    },
    buildInstancedFromAsset: function (THREE, chunkGrp, asset, transforms, scaleMult, tintHexList) {
      return buildInstancedFromAsset(THREE, chunkGrp, asset, transforms, scaleMult, tintHexList);
    },
    buildInstancedHexTiles: function (THREE, chunkGrp, asset, transforms, hexScale) {
      return buildInstancedHexTiles(THREE, chunkGrp, asset, transforms, hexScale);
    },
    removePlaceholderMeshes,
    registerBuildingMount,
    registerMachineMount,
    registerBiomeSceneryHook,
    ensureBiomeAssets,
    getDebugStats: function () {
      const ready = Object.keys(biomeState).filter((k) => biomeState[k] === "ready");
      return {
        gltfLoaderAvailable: Boolean(window.THREE && window.THREE.GLTFLoader),
        cachedAssets: assetCache.size,
        sharedTextures: textureCache.size,
        sharedMaterials: materialCache.size,
        readyBiomes: ready
      };
    }
  };
})();
