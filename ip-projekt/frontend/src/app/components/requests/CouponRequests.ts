import { API_BASE_URL } from "./baseUrl";
import axios from "axios";


export const API_URL = `${API_BASE_URL}/coupon/`;



export const getAllCoupons = async () => {
    try {
        return axios.get(API_URL + "all", { withCredentials: true });
    } catch (error) {
        console.error('Error fetching coupons:', error);
        return null;
    }
}

export const deleteCoupon = async (couponName: string) => {
    try {
        return axios.delete(API_URL + couponName + "/delete" , { withCredentials: true });
    } catch (error) {
        console.error('Error deleting coupon:', error);
        return null;
    }
}

export const createCoupon = async (couponData: any) => {
    try {
        return axios.post(API_URL + "add", couponData, { withCredentials: true });
    } catch (error) {
        console.error('Error creating coupon:', error);
        return null;
    }
}