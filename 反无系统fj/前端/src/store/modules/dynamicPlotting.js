import { defineStore } from 'pinia';
import { store } from "@/store";

export const useDynamicPlotting = defineStore({
    id: "dynamic-plotting",
    state: () => ({
        show: false,
    }),
    actions: {
        changeShow(show) {
            //console.log(show,"78798789789789")
            this.show = show;
        }
    },
})

export function useDynamicPlottingStore() {
    return useDynamicPlotting(store);
}

