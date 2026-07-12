import { defineStore } from 'pinia';
import { store } from "@/store";

export const useKy = defineStore({
    id: "use-ky",
    state: () => ({
        ky: [],
    }),
    actions: {
        getKy(ky) {
            //console.log(show,"78798789789789")
            this.ky = ky;
        },
        clearky(){
            this.ky.forEach(entity=>{
                window.Map3D.viewer.entities.remove(entity);
            })
        }
    },
})

export function useKyStore() {
    return useKy(store);
}

