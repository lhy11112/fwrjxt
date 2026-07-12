import { defineStore } from 'pinia';
import { store } from "@/store";

export const useMap2D = defineStore({
    id: "map-2d",
    state: () => ({
        width: "100%",
        height: "100%",
        left: "0",
        top: '0',
        position:'absolute',
        show: true,
        province:"",
        PKTY_USER_INFO:{}
    }),
    actions: {
        changeWidthAndHeight(width, height) {
            console.log(width, height);
            this.width = width;
            this.height = height;
        },
        changeShow(show) {
            this.show = show;
        },
        changePosition(position) {
            this.position = position;
        },
        changeProvince(province) {
            this.province = province;
        },
        changePKTY_USER_INFO(PKTY_USER_INFO){
            this.PKTY_USER_INFO = PKTY_USER_INFO;
        }
    },
})

export function useMap2DStore() {
    return useMap2D(store);
}

