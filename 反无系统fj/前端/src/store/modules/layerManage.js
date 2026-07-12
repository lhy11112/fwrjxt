import { defineStore } from 'pinia';
import { store } from "@/store";

export const useLayerManage = defineStore({
    id: "layer-manage",
    state: () => ({
        show: false,
        rightLayer:null
    }),
    actions: {
        changeShow(show) {
            this.show = show;
        },
        getLayerRightClick(item){
            this.rightLayer=item

        }
    },
})

export function useLayerManageStore() {
    return useLayerManage(store);
}

