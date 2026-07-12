import { defineStore } from 'pinia';
import { store } from "@/store";

export const useCoord = defineStore({
    id: "coord",
    state: () => ({
        x: 0,
        y: 0,
        z: 0,
        zoom:0,
    }),
    actions: {
        setCoord(coord) {
            this.x = coord.x;
            this.y = coord.y;
            this.z = coord.z;
            this.zoom=coord.zoom;
        }
    }
})

export function useCoordStore() {
    return useCoord(store);
}

