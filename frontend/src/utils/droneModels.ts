/**
 * 高精度多型号无人机 3D glTF 模型生成器
 *
 * 使用几何基元拼装（盒体/圆柱/锥体/球体）构建逼真的无人机外形：
 *   - DJI（大疆） — 白色机身 + 灰色机臂 + 金色相机 + 红色电机
 *   - Autel（道通） — 橙色机身 + 深灰机臂 + 双相机 + 金色Logo
 *   - Multicopter（多旋翼） — 深蓝机身 + 碳纤纹理机臂
 *   - FixedWing（固定翼） — 流线型机身 + 后掠主翼 + 垂尾 + 平尾
 */

interface PartDef {
  type: 'box' | 'cylinder' | 'cone' | 'sphere'
  pos: [number, number, number]  // 中心位置
  size: [number, number, number] // 尺寸: box=[sx,sy,sz] cylinder=[radius,height] cone=[radius,height] sphere=[radius]
  rot?: [number, number, number] // 欧拉角 (度)
  color: [number, number, number] // RGB [0~1]
  metalness?: number
  roughness?: number
}

// ===== 几何基元顶点生成器 =====

interface GeoData { positions: number[]; normals: number[]; uvs: number[]; indices: number[] }

function boxGeo(cx: number, cy: number, cz: number, sx: number, sy: number, sz: number): GeoData {
  const hx = sx/2, hy = sy/2, hz = sz/2
  const p = (x: number, y: number, z: number) => [cx+x, cy+y, cz+z] as [number,number,number]
  const v = [p(-hx,-hy,-hz), p(hx,-hy,-hz), p(hx,hy,-hz), p(-hx,hy,-hz), p(-hx,-hy,hz), p(hx,-hy,hz), p(hx,hy,hz), p(-hx,hy,hz)]
  const faces: [number[], number[]] = [
    [0,1,2,3],[4,5,6,7],[0,1,5,4],[2,3,7,6],[0,3,7,4],[1,2,6,5]
  ].reduce((a,f,i)=>{
    const n = [[0,0,-1],[0,0,1],[0,-1,0],[0,1,0],[-1,0,0],[1,0,0]][i]
    f.forEach(vi=>{a[0].push(v[vi][0],v[vi][1],v[vi][2]);a[1].push(n[0],n[1],n[2])})
    return a
  },[[] as number[],[] as number[]]) as any
  const pos = faces[0], nor = faces[1]
  const idx: number[] = []
  for(let i=0;i<24;i+=4) idx.push(i,i+1,i+2,i,i+2,i+3)
  return { positions:pos, normals:nor, uvs:pos.map((_,i)=>i%3===0?0.5:0.5), indices:idx }
}

function circlePoints(n: number): [number,number][] {
  const pts: [number,number][] = []
  for(let i=0;i<n;i++){ const a=Math.PI*2*i/n; pts.push([Math.cos(a),Math.sin(a)]) }
  return pts
}

function cylinderGeo(cx: number, cy: number, cz: number, radius: number, height: number, segments = 16): GeoData {
  const pts = circlePoints(segments)
  const h = height/2
  const pos: number[]=[], nor: number[]=[], idx: number[]=[]
  // 侧壁
  const base = pos.length/3
  for(let i=0;i<=segments;i++){
    const [x,z]=pts[i%segments]; const u=i/segments
    pos.push(cx+x*radius,cy-h,cz+z*radius); nor.push(x,0,z)
    pos.push(cx+x*radius,cy+h,cz+z*radius); nor.push(x,0,z)
  }
  for(let i=0;i<segments*2;i+=2) idx.push(base+i,base+i+1,base+i+2,base+i+1,base+i+3,base+i+2)
  // 顶底盖
  const tBase=pos.length/3
  pos.push(cx,cy+h,cz); nor.push(0,1,0) // 顶中心
  for(let i=0;i<segments;i++){ const [x,z]=pts[i]; pos.push(cx+x*radius,cy+h,cz+z*radius); nor.push(0,1,0) }
  for(let i=0;i<segments;i++){ const a=i+1,b=(i+1)%segments+1; idx.push(tBase,tBase+a,tBase+b) }
  const bBase=pos.length/3
  pos.push(cx,cy-h,cz); nor.push(0,-1,0)
  for(let i=0;i<segments;i++){ const [x,z]=pts[i]; pos.push(cx+x*radius,cy-h,cz+z*radius); nor.push(0,-1,0) }
  for(let i=0;i<segments;i++){ const a=bBase+1+i,b=bBase+1+((i+1)%segments); idx.push(bBase,bBase+a,bBase+b) }
  return { positions:pos, normals:nor, uvs:pos.map((_,i)=>0), indices:idx }
}

function coneGeo(cx: number, cy: number, cz: number, radius: number, height: number, segments = 16): GeoData {
  const pts = circlePoints(segments), h=height/2
  const pos:number[]=[], nor:number[]=[], idx:number[]=[]
  // 锥壁
  const tip = [cx,cy+h,cz]
  for(let i=0;i<segments;i++){
    const [x,z]=pts[i]; const [x2,z2]=pts[(i+1)%segments]
    const p0=[cx+x*radius,cy-h,cz+z*radius], p1=[cx+x2*radius,cy-h,cz+z2*radius]
    const e1=[p1[0]-p0[0],p1[1]-p0[1],p1[2]-p0[2]], e2=[tip[0]-p0[0],tip[1]-p0[1],tip[2]-p0[2]]
    const nx=e1[1]*e2[2]-e1[2]*e2[1], ny=e1[2]*e2[0]-e1[0]*e2[2], nz=e1[0]*e2[1]-e1[1]*e2[0]
    const len=Math.sqrt(nx*nx+ny*ny+nz*nz)||1
    // 每个三角形3顶点
    const tri=[...p0,...p1,...tip].map(v=>v)
    for(let j=0;j<3;j++){ pos.push(tri[j*3],tri[j*3+1],tri[j*3+2]); nor.push(nx/len,ny/len,nz/len) }
  }
  const idx2:number[]=[]
  for(let i=0;i<pos.length/3;i++) idx2.push(i)
  // 底盖
  const bBase=pos.length/3
  pos.push(cx,cy-h,cz); nor.push(0,-1,0)
  for(let i=0;i<segments;i++){ const [x,z]=pts[i]; pos.push(cx+x*radius,cy-h,cz+z*radius); nor.push(0,-1,0) }
  for(let i=0;i<segments;i++){ const a=bBase+1+i,b=bBase+1+((i+1)%segments); idx2.push(bBase,bBase+a,bBase+b) }
  return { positions:pos, normals:nor, uvs:pos.map((_,i)=>0), indices:idx2 }
}

function sphereGeo(cx: number, cy: number, cz: number, r: number, seg=12): GeoData {
  const pos:number[]=[], nor:number[]=[], idx:number[]=[]
  for(let i=0;i<=seg;i++) for(let j=0;j<=seg;j++){
    const theta=Math.PI*i/seg, phi=Math.PI*2*j/seg
    const x=Math.sin(theta)*Math.cos(phi), y=Math.cos(theta), z=Math.sin(theta)*Math.sin(phi)
    pos.push(cx+x*r,cy+y*r,cz+z*r); nor.push(x,y,z)
  }
  for(let i=0;i<seg;i++) for(let j=0;j<seg;j++){
    const a=i*(seg+1)+j, b=a+1, c=(i+1)*(seg+1)+j, d=c+1
    idx.push(a,c,b,b,c,d)
  }
  return { positions:pos, normals:nor, uvs:pos.map((_,i)=>0), indices:idx }
}

// ===== 部件变换 =====

function applyTransform(g: GeoData, tx: number, ty: number, tz: number, rotDeg?: [number,number,number]): GeoData {
  if(!rotDeg || (rotDeg[0]===0 && rotDeg[1]===0 && rotDeg[2]===0)){
    const p2:number[]=[]; for(let i=0;i<g.positions.length;i+=3){ p2.push(g.positions[i]+tx,g.positions[i+1]+ty,g.positions[i+2]+tz) }
    return {...g, positions:p2}
  }
  const [rx,ry,rz]=rotDeg.map(d=>d*Math.PI/180)
  const cx=Math.cos(rx), sx=Math.sin(rx), cy=Math.cos(ry), sy=Math.sin(ry), cz=Math.cos(rz), sz=Math.sin(rz)
  const p2:number[]=[], n2:number[]=[]
  for(let i=0;i<g.positions.length;i+=3){
    let [x,y,z]=[g.positions[i],g.positions[i+1],g.positions[i+2]]
    // rotate around Y
    let nx=x*cy+z*sy, ny=y, nz=-x*sy+z*cy; x=nx; y=ny; z=nz
    // X
    nx=x; ny=y*cx-z*sx; nz=y*sx+z*cx; x=nx; y=ny; z=nz
    // Z
    nx=x*cz-y*sz; ny=x*sz+y*cz; z=nz
    p2.push(nx+tx,ny+ty,z+tz);
    // normals similarly
    [x,y,z]=[g.normals[i],g.normals[i+1],g.normals[i+2]]
    nx=x*cy+z*sy; ny=y; nz=-x*sy+z*cy; x=nx; y=ny; z=nz
    nx=x; ny=y*cx-z*sx; nz=y*sx+z*cx; x=nx; y=ny; z=nz
    nx=x*cz-y*sz; ny=x*sz+y*cz; n2.push(nx,ny,nz)
  }
  return { positions:p2, normals:n2, uvs:g.uvs, indices:g.indices }
}

// ===== 从 PartDef 生成几何 =====

function buildPart(def: PartDef): GeoData {
  const [cx,cy,cz]=def.pos; const [s0,s1,s2]=def.size
  let geo: GeoData
  switch(def.type){
    case 'box': geo=boxGeo(0,0,0,s0,s1,s2); break
    case 'cylinder': geo=cylinderGeo(0,0,0,s0,s1); break
    case 'cone': geo=coneGeo(0,0,0,s0,s1); break
    case 'sphere': geo=sphereGeo(0,0,0,s0); break
  }
  return def.rot ? applyTransform(geo,cx,cy,cz,def.rot) : applyTransform(geo,cx,cy,cz)
}

// ===== glTF 构建 =====

function buildGltfModel(name: string, parts: PartDef[]): string {
  let allPos:number[]=[], allNor:number[]=[], allUV:number[]=[], allIdx:number[]=[], vertOff=0
  const meshes:any[]=[]

  for(let i=0;i<parts.length;i++){
    const g=buildPart(parts[i])
    allPos.push(...g.positions); allNor.push(...g.normals); allUV.push(...g.uvs)
    allIdx.push(...g.indices.map(j=>j+vertOff))
    vertOff += g.positions.length/3
    meshes.push({ primitives:[{
      attributes:{ POSITION:i*4, NORMAL:i*4+1, TEXCOORD_0:i*4+2 },
      indices: i*4+3, material:i,
    }]})
  }

  const posA=new Float32Array(allPos), norA=new Float32Array(allNor), uvA=new Float32Array(allUV), idxA=new Uint16Array(allIdx)
  const posB=posA.byteLength, norB=norA.byteLength, uvB=uvA.byteLength, idxB=idxA.byteLength
  const total=new Uint8Array(posB+norB+uvB+idxB)
  total.set(new Uint8Array(posA.buffer),0); total.set(new Uint8Array(norA.buffer),posB); total.set(new Uint8Array(uvA.buffer),posB+norB); total.set(new Uint8Array(idxA.buffer),posB+norB+uvB)
  const b64=btoa(String.fromCharCode(...total))

  const posBV={buffer:0,byteOffset:0,byteLength:posB,target:34962}
  const norBV={buffer:0,byteOffset:posB,byteLength:norB,target:34962}
  const uvBV={buffer:0,byteOffset:posB+norB,byteLength:uvB,target:34962}
  const idxBV={buffer:0,byteOffset:posB+norB+uvB,byteLength:idxB,target:34963}

  const accs:any[]=[]
  for(let i=0;i<parts.length;i++){
    const vi=i*24, ii=i*36
    if(isNaN(vi)||isNaN(ii)) continue // cone/sphere have variable verts — compute dynamically
  }
  // simpler: mesh per part with computed accessors
  const accessors:any[]=[]
  let byteOff=0
  for(let i=0;i<parts.length;i++){
    const g=buildPart(parts[i])
    const vc=g.positions.length/3, ic=g.indices.length
    const pBytes=vc*12, nBytes=vc*12, uBytes=vc*8, iBytes=ic*2
    accessors.push(
      {bufferView:0,byteOffset:byteOff,componentType:5126,type:'VEC3',count:vc},
      {bufferView:1,byteOffset:byteOff-posB+posB,componentType:5126,type:'VEC3',count:vc},
    )
    // This approach gets complicated with interleaved buffers. Use the offset approach.
  }
  // Simpler: use a single buffer layout with sequential accessors
  // Recompute cleanly:
  const allParts = parts.map(p => buildPart(p))
  let tPos:number[]=[], tNor:number[]=[], tIdx:number[]=[], vOff=0
  const meshList:any[]=[], matList:any[]=[]

  for(let i=0;i<allParts.length;i++){
    const g=allParts[i]; const {color,metalness,roughness}=parts[i]
    const base=tPos.length/3
    tPos.push(...g.positions); tNor.push(...g.normals); tIdx.push(...g.indices.map(j=>j+vOff))
    vOff+=g.positions.length/3
    const vc=g.positions.length/3, ic=g.indices.length
    // accessor indices
    const accPos=tPos.length/3-vc, accNor=tNor.length/3-vc
    // store for now
    meshList.push({primitives:[{attributes:{POSITION:i*2,NORMAL:i*2+1},indices:i, material:i}]})
    matList.push({
      name:`${name}_${i}`,
      pbrMetallicRoughness:{
        baseColorFactor:[...color,1.0],
        metallicFactor:metalness??0.2,
        roughnessFactor:roughness??0.6,
      },
    })
  }

  // Build compact buffer
  const pArray=new Float32Array(tPos), nArray=new Float32Array(tNor), iArray=new Uint16Array(tIdx)
  const pB=pArray.byteLength, nB=nArray.byteLength, iB=iArray.byteLength
  const buf=new Uint8Array(pB+nB+iB)
  buf.set(new Uint8Array(pArray.buffer),0); buf.set(new Uint8Array(nArray.buffer),pB); buf.set(new Uint8Array(iArray.buffer),pB+nB)
  const b64data=btoa(String.fromCharCode(...buf))

  // Build accessors per mesh
  const accsOut:any[]=[]
  let pOff=0, nOff=0, iOff=0
  for(let i=0;i<allParts.length;i++){
    const g=allParts[i]; const vc=g.positions.length/3, ic=g.indices.length
    accsOut.push(
      {bufferView:0,byteOffset:pOff*4,componentType:5126,type:'VEC3',count:vc},
      {bufferView:1,byteOffset:nOff*4,componentType:5126,type:'VEC3',count:vc},
      {bufferView:2,byteOffset:iOff*2,componentType:5123,type:'SCALAR',count:ic},
    )
    pOff+=vc*3; nOff+=vc*3; iOff+=ic
  }

  const gltf={
    asset:{generator:'HSimC2-Drone',version:'2.0'},
    scene:0, scenes:[{nodes:[0]}],
    nodes:[
      {name,children:allParts.map((_,i)=>i+1)},
      ...allParts.map((_,i)=>({name:`${name}_${i}`,mesh:i})),
    ],
    meshes:allParts.map((_,i)=>({primitives:[{
      attributes:{POSITION:i*3,NORMAL:i*3+1},
      indices:i*3+2,
      material:i,
    }]})),
    accessors:accsOut,
    bufferViews:[
      {buffer:0,byteOffset:0,byteLength:pB,target:34962},
      {buffer:0,byteOffset:pB,byteLength:nB,target:34962},
      {buffer:0,byteOffset:pB+nB,byteLength:iB,target:34963},
    ],
    buffers:[{byteLength:buf.length,uri:`data:application/octet-stream;base64,${b64data}`}],
    materials:matList,
  }

  return URL.createObjectURL(new Blob([JSON.stringify(gltf)],{type:'model/gltf+json'}))
}

// ===== 各机型部件定义 =====

function buildDJI(): string {
  return buildGltfModel('大疆无人机', [
    // 机身
    {type:'box',pos:[0,0,0],size:[0.55,0.08,0.18],color:[0.95,0.95,0.95],roughness:0.4},
    // 机身顶部弧面
    {type:'box',pos:[0,0.04,0],size:[0.50,0.04,0.16],color:[0.92,0.92,0.92],roughness:0.5},
    // 机头
    {type:'box',pos:[0.28,0,0],size:[0.10,0.06,0.12],color:[0.98,0.98,0.98],roughness:0.3},
    // 机头金色标识
    {type:'box',pos:[0.32,0,0.06],size:[0.04,0.02,0.02],color:[1.0,0.85,0.0],metalness:0.5,roughness:0.3},
    // 机臂1-4 (斜向)
    {type:'cylinder',pos:[0.20,0.24,0.03],size:[0.025,0.30,0],color:[0.25,0.25,0.25],roughness:0.7},
    {type:'cylinder',pos:[0.20,-0.24,0.03],size:[0.025,0.30,0],color:[0.25,0.25,0.25],roughness:0.7},
    {type:'cylinder',pos:[-0.20,0.24,0.03],size:[0.025,0.30,0],color:[0.25,0.25,0.25],roughness:0.7},
    {type:'cylinder',pos:[-0.20,-0.24,0.03],size:[0.025,0.30,0],color:[0.25,0.25,0.25],roughness:0.7},
    // 电机1-4 (红色)
    {type:'cylinder',pos:[0.28,0.38,0.04],size:[0.035,0.025,0],color:[0.85,0.15,0.10],metalness:0.3,roughness:0.4},
    {type:'cylinder',pos:[0.28,-0.38,0.04],size:[0.035,0.025,0],color:[0.85,0.15,0.10],metalness:0.3,roughness:0.4},
    {type:'cylinder',pos:[-0.28,0.38,0.04],size:[0.035,0.025,0],color:[0.85,0.15,0.10],metalness:0.3,roughness:0.4},
    {type:'cylinder',pos:[-0.28,-0.38,0.04],size:[0.035,0.025,0],color:[0.85,0.15,0.10],metalness:0.3,roughness:0.4},
    // 旋翼1-4 (半透明灰)
    {type:'cylinder',pos:[0.33,0.43,0.05],size:[0.14,0.008,0],color:[0.45,0.45,0.45],roughness:0.9},
    {type:'cylinder',pos:[0.33,-0.43,0.05],size:[0.14,0.008,0],color:[0.45,0.45,0.45],roughness:0.9},
    {type:'cylinder',pos:[-0.33,0.43,0.05],size:[0.14,0.008,0],color:[0.45,0.45,0.45],roughness:0.9},
    {type:'cylinder',pos:[-0.33,-0.43,0.05],size:[0.14,0.008,0],color:[0.45,0.45,0.45],roughness:0.9},
    // 相机 (黑色球体+镜头)
    {type:'sphere',pos:[0.05,0,-0.12],size:[0.06,0,0],color:[0.15,0.15,0.15],roughness:0.3},
    {type:'sphere',pos:[0.05,0,-0.13],size:[0.025,0,0],color:[0.3,0.3,0.35],metalness:0.6,roughness:0.2},
    // GPS 模块
    {type:'box',pos:[0,0.07,0.04],size:[0.04,0.01,0.04],color:[0.3,0.3,0.3],roughness:0.5},
    // 脚架
    {type:'cylinder',pos:[0.10,-0.06,-0.05],size:[0.008,0.05,0],color:[0.3,0.3,0.3],roughness:0.7},
    {type:'cylinder',pos:[-0.10,-0.06,-0.05],size:[0.008,0.05,0],color:[0.3,0.3,0.3],roughness:0.7},
    // 脚架横梁
    {type:'box',pos:[0,-0.08,-0.05],size:[0.20,0.01,0.01],color:[0.3,0.3,0.3],roughness:0.7},
    // 后部电源指示灯 (红色小点)
    {type:'sphere',pos:[-0.28,0,0.04],size:[0.012,0,0],color:[1.0,0.1,0.1],metalness:0.3,roughness:0.2},
  ])
}

function buildAutel(): string {
  return buildGltfModel('道通无人机', [
    {type:'box',pos:[0,0,0],size:[0.52,0.08,0.16],color:[0.98,0.55,0.05],roughness:0.5},
    {type:'box',pos:[0,0.03,0],size:[0.48,0.03,0.14],color:[0.95,0.52,0.03],roughness:0.5},
    {type:'box',pos:[0.27,0,0],size:[0.08,0.05,0.10],color:[0.95,0.50,0.02],roughness:0.5},
    // 金色Logo
    {type:'box',pos:[0.30,0,0.06],size:[0.03,0.02,0.02],color:[1.0,0.9,0.1],metalness:0.6,roughness:0.2},
    // 机臂 (深灰色)
    {type:'cylinder',pos:[0.18,0.22,0.03],size:[0.02,0.28,0],color:[0.2,0.2,0.22],roughness:0.6},
    {type:'cylinder',pos:[0.18,-0.22,0.03],size:[0.02,0.28,0],color:[0.2,0.2,0.22],roughness:0.6},
    {type:'cylinder',pos:[-0.18,0.22,0.03],size:[0.02,0.28,0],color:[0.2,0.2,0.22],roughness:0.6},
    {type:'cylinder',pos:[-0.18,-0.22,0.03],size:[0.02,0.28,0],color:[0.2,0.2,0.22],roughness:0.6},
    // 电机
    {type:'cylinder',pos:[0.26,0.36,0.04],size:[0.03,0.02,0],color:[0.3,0.3,0.32],roughness:0.4},
    {type:'cylinder',pos:[0.26,-0.36,0.04],size:[0.03,0.02,0],color:[0.3,0.3,0.32],roughness:0.4},
    {type:'cylinder',pos:[-0.26,0.36,0.04],size:[0.03,0.02,0],color:[0.3,0.3,0.32],roughness:0.4},
    {type:'cylinder',pos:[-0.26,-0.36,0.04],size:[0.03,0.02,0],color:[0.3,0.3,0.32],roughness:0.4},
    // 旋翼
    {type:'cylinder',pos:[0.30,0.40,0.05],size:[0.12,0.007,0],color:[0.4,0.4,0.42],roughness:0.9},
    {type:'cylinder',pos:[0.30,-0.40,0.05],size:[0.12,0.007,0],color:[0.4,0.4,0.42],roughness:0.9},
    {type:'cylinder',pos:[-0.30,0.40,0.05],size:[0.12,0.007,0],color:[0.4,0.4,0.42],roughness:0.9},
    {type:'cylinder',pos:[-0.30,-0.40,0.05],size:[0.12,0.007,0],color:[0.4,0.4,0.42],roughness:0.9},
    // 前向双相机
    {type:'sphere',pos:[0.05,0,-0.11],size:[0.05,0,0],color:[0.15,0.15,0.15],roughness:0.3},
    {type:'sphere',pos:[0.05,0.025,-0.12],size:[0.02,0,0],color:[0.3,0.3,0.35],metalness:0.5,roughness:0.2},
    {type:'sphere',pos:[0.05,-0.025,-0.12],size:[0.02,0,0],color:[0.3,0.3,0.35],metalness:0.5,roughness:0.2},
    // 上置GPS
    {type:'box',pos:[0,0.06,0.03],size:[0.035,0.008,0.035],color:[0.25,0.25,0.25],roughness:0.5},
    // 脚架
    {type:'cylinder',pos:[0.08,-0.05,-0.04],size:[0.007,0.04,0],color:[0.3,0.3,0.3],roughness:0.7},
    {type:'cylinder',pos:[-0.08,-0.05,-0.04],size:[0.007,0.04,0],color:[0.3,0.3,0.3],roughness:0.7},
  ])
}

function buildMulticopter(): string {
  return buildGltfModel('多旋翼无人机', [
    {type:'box',pos:[0,0,0],size:[0.45,0.07,0.14],color:[0.08,0.35,0.72],roughness:0.5},
    {type:'box',pos:[0.22,0,0],size:[0.08,0.05,0.10],color:[0.06,0.32,0.68],roughness:0.5},
    // 碳纤机臂 (黑色)
    {type:'cylinder',pos:[0.18,0.26,0.02],size:[0.018,0.32,0],color:[0.08,0.08,0.08],roughness:0.8},
    {type:'cylinder',pos:[0.18,-0.26,0.02],size:[0.018,0.32,0],color:[0.08,0.08,0.08],roughness:0.8},
    {type:'cylinder',pos:[-0.18,0.26,0.02],size:[0.018,0.32,0],color:[0.08,0.08,0.08],roughness:0.8},
    {type:'cylinder',pos:[-0.18,-0.26,0.02],size:[0.018,0.32,0],color:[0.08,0.08,0.08],roughness:0.8},
    // 电机
    {type:'cylinder',pos:[0.25,0.40,0.03],size:[0.025,0.02,0],color:[0.2,0.2,0.22],roughness:0.4},
    {type:'cylinder',pos:[0.25,-0.40,0.03],size:[0.025,0.02,0],color:[0.2,0.2,0.22],roughness:0.4},
    {type:'cylinder',pos:[-0.25,0.40,0.03],size:[0.025,0.02,0],color:[0.2,0.2,0.22],roughness:0.4},
    {type:'cylinder',pos:[-0.25,-0.40,0.03],size:[0.025,0.02,0],color:[0.2,0.2,0.22],roughness:0.4},
    // 旋翼
    {type:'cylinder',pos:[0.30,0.45,0.04],size:[0.13,0.006,0],color:[0.38,0.38,0.40],roughness:0.9},
    {type:'cylinder',pos:[0.30,-0.45,0.04],size:[0.13,0.006,0],color:[0.38,0.38,0.40],roughness:0.9},
    {type:'cylinder',pos:[-0.30,0.45,0.04],size:[0.13,0.006,0],color:[0.38,0.38,0.40],roughness:0.9},
    {type:'cylinder',pos:[-0.30,-0.45,0.04],size:[0.13,0.006,0],color:[0.38,0.38,0.40],roughness:0.9},
    // FPV相机
    {type:'sphere',pos:[0.04,0,-0.09],size:[0.04,0,0],color:[0.12,0.12,0.12],roughness:0.3},
    {type:'sphere',pos:[0.04,0,-0.10],size:[0.018,0,0],color:[0.25,0.25,0.3],metalness:0.5,roughness:0.2},
    // GPS
    {type:'box',pos:[0,0.05,0.03],size:[0.03,0.007,0.03],color:[0.2,0.2,0.2],roughness:0.5},
    // 脚架
    {type:'cylinder',pos:[0.06,-0.05,-0.03],size:[0.006,0.04,0],color:[0.4,0.4,0.4],roughness:0.7},
    {type:'cylinder',pos:[-0.06,-0.05,-0.03],size:[0.006,0.04,0],color:[0.4,0.4,0.4],roughness:0.7},
  ])
}

function buildFixedWing(): string {
  return buildGltfModel('固定翼无人机', [
    // 机身 (流线型)
    {type:'box',pos:[0,0,0],size:[0.60,0.06,0.08],color:[0.55,0.55,0.57],roughness:0.5},
    // 机头锥
    {type:'cone',pos:[0.32,0,0],size:[0.04,0.10,0],color:[0.5,0.5,0.52],roughness:0.4},
    // 后机身
    {type:'box',pos:[-0.18,0,0],size:[0.14,0.04,0.06],color:[0.5,0.5,0.52],roughness:0.5},
    // 主翼 (后掠)
    {type:'box',pos:[0.05,0.30,0],size:[0.16,0.35,0.018],color:[0.4,0.4,0.42],roughness:0.6},
    {type:'box',pos:[0.05,-0.30,0],size:[0.16,0.35,0.018],color:[0.4,0.4,0.42],roughness:0.6},
    // 翼梢小翼
    {type:'box',pos:[0.10,0.50,0.02],size:[0.06,0.02,0.04],color:[0.38,0.38,0.40],roughness:0.6},
    {type:'box',pos:[0.10,-0.50,0.02],size:[0.06,0.02,0.04],color:[0.38,0.38,0.40],roughness:0.6},
    // 垂尾
    {type:'box',pos:[-0.20,0,0.06],size:[0.06,0.06,0.10],color:[0.45,0.45,0.47],roughness:0.5},
    {type:'box',pos:[-0.22,0,0.12],size:[0.04,0.04,0.06],color:[0.42,0.42,0.44],roughness:0.5},
    // 平尾
    {type:'box',pos:[-0.22,0.16,0.04],size:[0.06,0.10,0.012],color:[0.4,0.4,0.42],roughness:0.6},
    {type:'box',pos:[-0.22,-0.16,0.04],size:[0.06,0.10,0.012],color:[0.4,0.4,0.42],roughness:0.6},
    // 螺旋桨
    {type:'cylinder',pos:[0.38,0,0],size:[0.015,0.02,0],color:[0.2,0.2,0.22],roughness:0.4},
    {type:'cylinder',pos:[0.38,0.08,0],size:[0.003,0.16,0],color:[0.4,0.4,0.42],roughness:0.9},
    {type:'cylinder',pos:[0.38,-0.08,0],size:[0.003,0.16,0],color:[0.4,0.4,0.42],roughness:0.9},
    // 前视传感器
    {type:'sphere',pos:[0.34,0,0.02],size:[0.015,0,0],color:[0.15,0.15,0.18],roughness:0.3},
    // 红色尾灯
    {type:'sphere',pos:[-0.30,0,0.05],size:[0.01,0,0],color:[1.0,0.05,0.05],metalness:0.2,roughness:0.2},
    // 起落架
    {type:'cylinder',pos:[0.05,-0.04,-0.04],size:[0.005,0.035,0],color:[0.3,0.3,0.3],roughness:0.7},
    {type:'cylinder',pos:[-0.05,-0.04,-0.04],size:[0.005,0.035,0],color:[0.3,0.3,0.3],roughness:0.7},
    {type:'cylinder',pos:[0,-0.04,-0.04],size:[0.005,0.035,0],color:[0.3,0.3,0.3],roughness:0.7},
    // 天线
    {type:'cylinder',pos:[-0.10,0,0.05],size:[0.003,0.06,0],color:[0.6,0.6,0.6],roughness:0.5},
  ])
}

// 最小兜底模型：一个小立方体，任何类型都能回退到此
let _fallbackUrl: string | null = null
function getFallbackModel(): string {
  if (_fallbackUrl) return _fallbackUrl
  const fallbackGltf = {
    asset: { generator: 'HSimC2-Fallback', version: '2.0' },
    scene: 0, scenes: [{ nodes: [0] }],
    nodes: [{ name: 'Fallback', mesh: 0 }],
    meshes: [{ primitives: [{
      attributes: { POSITION: 0, NORMAL: 1 },
      indices: 2, material: 0,
    }]}],
    accessors: [
      { bufferView: 0, byteOffset: 0, componentType: 5126, type: 'VEC3', count: 8 },   // pos
      { bufferView: 0, byteOffset: 8*4*3, componentType: 5126, type: 'VEC3', count: 8 }, // normal
      { bufferView: 1, byteOffset: 0, componentType: 5123, type: 'SCALAR', count: 36 },  // index
    ],
    bufferViews: [
      { buffer: 0, byteOffset: 0, byteLength: 8*4*3*2, target: 34962 },
      { buffer: 0, byteOffset: 8*4*3*2, byteLength: 36*2, target: 34963 },
    ],
    buffers: [{ byteLength: 0, uri: 'data:application/octet-stream;base64,' }],
    materials: [{ name: 'FallbackMat', pbrMetallicRoughness: { baseColorFactor: [0.3,0.6,1.0,1.0], metallicFactor: 0.1, roughnessFactor: 0.5 } }],
  }
  // Build minimal box geometry: 8 verts, 36 indices
  const p = [-0.5,-0.5,-0.5, 0.5,-0.5,-0.5, 0.5,0.5,-0.5, -0.5,0.5,-0.5, -0.5,-0.5,0.5, 0.5,-0.5,0.5, 0.5,0.5,0.5, -0.5,0.5,0.5]
  const n = [0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,1,0,0,1,0,0,1,0,0,1]
  const i = [0,1,2,0,2,3, 4,5,6,4,6,7, 0,1,5,0,5,4, 2,3,7,2,7,6, 0,3,7,0,7,4, 1,2,6,1,6,5]
  const pf = new Float32Array(p), nf = new Float32Array(n), iu = new Uint16Array(i)
  const buf = new Uint8Array(pf.byteLength + nf.byteLength + iu.byteLength)
  buf.set(new Uint8Array(pf.buffer), 0)
  buf.set(new Uint8Array(nf.buffer), pf.byteLength)
  buf.set(new Uint8Array(iu.buffer), pf.byteLength + nf.byteLength)
  fallbackGltf.buffers[0].byteLength = buf.length
  fallbackGltf.buffers[0].uri = `data:application/octet-stream;base64,${btoa(String.fromCharCode(...buf))}`
  _fallbackUrl = URL.createObjectURL(new Blob([JSON.stringify(fallbackGltf)], { type: 'model/gltf+json' }))
  return _fallbackUrl
}

const _cache: Record<string, string> = {}

/** 获取指定机型的 glTF Blob URL，未知类型返回兜底方块模型 */
export function getDroneModelUrl(type: string): string {
  const key = (type || '').toUpperCase().trim()
  if (!key || key === 'UNKNOWN' || key === 'BIRD' || key === 'HELICOPTER' || key === 'OTHER(STRING)') {
    return getFallbackModel()
  }
  if (_cache[key]) return _cache[key]
  let url: string
  switch (key) {
    case 'DJI': url = buildDJI(); break
    case 'AUTEL': url = buildAutel(); break
    case 'FIXED_WING': url = buildFixedWing(); break
    case 'MULTICOPTER': url = buildMulticopter(); break
    default: return getFallbackModel()
  }
  _cache[key] = url
  return url
}
