define(["./CompressedTextureBuffer-4a8a4916","./when-8feb740e","./PixelFormat-4f616604","./RuntimeError-3d89a7bb","./createTaskProcessorWorker","./WebGLConstants-71f10989"],(function(e,r,t,n,a,s){"use strict";
/**
     * @license
     *
     * Copyright (c) 2014, Brandon Jones. All rights reserved.
     *
     * Redistribution and use in source and binary forms, with or without modification,
     * are permitted provided that the following conditions are met:
     *
     *  * Redistributions of source code must retain the above copyright notice, this
     *  list of conditions and the following disclaimer.
     *  * Redistributions in binary form must reproduce the above copyright notice,
     *  this list of conditions and the following disclaimer in the documentation
     *  and/or other materials provided with the distribution.
     *
     * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
     * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
     * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
     * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
     * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
     * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
     * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
     * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
     * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     */var f,o,i=1,u=2,_={};_[0]=t.PixelFormat.RGB_DXT1,_[i]=t.PixelFormat.RGBA_DXT3,_[u]=t.PixelFormat.RGBA_DXT5;var c,l=0;function m(a,s){var i=a.data,u=i.byteLength,m=new Uint8Array(i),b=c._malloc(u);!function(e,r,t,n){var a,s=t/4,f=n%4,o=new Uint32Array(e.buffer,0,(n-f)/4),i=new Uint32Array(r.buffer);for(a=0;a<o.length;a++)i[s+a]=o[a];for(a=n-f;a<n;a++)r[t+a]=e[a]}(m,c.HEAPU8,b,u);var d=c._crn_get_dxt_format(b,u),w=_[d];if(!r.e(w))throw new n.t("Unsupported compressed format.");var p,y=c._crn_get_levels(b,u),g=c._crn_get_width(b,u),h=c._crn_get_height(b,u),v=0;for(p=0;p<y;++p)v+=t.PixelFormat.compressedTextureSizeInBytes(w,g>>p,h>>p);if(l<v&&(r.e(f)&&c._free(f),f=c._malloc(v),o=new Uint8Array(c.HEAPU8.buffer,f,v),l=v),c._crn_decompress(b,u,f,v,0,y),c._free(b),r.u(a.bMipMap,!1)){var x=o.slice(0,v);return s.push(x.buffer),new e.e(w,g,h,x)}var A=t.PixelFormat.compressedTextureSizeInBytes(w,g,h),P=o.subarray(0,A),U=new Uint8Array(A);return U.set(P,0),s.push(U.buffer),new e.e(w,g,h,U)}function b(e){c=e,self.onmessage=a(m),self.postMessage(!0)}return function(e){var t=e.data.webAssemblyConfig;if(r.e(t))return require([t.modulePath],(function(e){r.e(t.wasmBinaryFile)?(r.e(e)||(e=self.Module),b(e)):b(e)}))}}));
