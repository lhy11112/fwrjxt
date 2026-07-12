import { defineStore } from 'pinia';
import { store } from "@/store";

export const useMap3D = defineStore({
    id: "map-3d",
    state: () => ({
        width: "0",
        height: "0",
        left: "0",
        top: '0',
        show: true
    }),
    actions: {
        changeWidthAndHeight(width, height) {
            console.log(width, height);
            this.width = width;
            this.height = height;
        },
        changeShow(show) {
            this.show = show;
        }
    },
})

export function useMap3DStore() {
    return useMap3D(store);
}

