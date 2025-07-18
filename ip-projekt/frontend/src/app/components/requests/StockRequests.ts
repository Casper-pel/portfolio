import axios from "axios";
import { API_BASE_URL } from "./baseUrl";
import { StockDto } from "../model/stock";

const API_URL = `${API_BASE_URL}/stock/`;

/**
 * fetches stock with id from api
 */
export const getStockById = async (id: string) => {
  try {
    return axios.get(API_URL + id, { withCredentials: true });
  } catch (error) {
    console.error("Error fetching stock by ID:", error);
    return null;
  }
};

/**
 * add new init stock to product
 * @param stock
 */
export const addStock = async (stock: StockDto) => {
  try {
    return axios.post(API_URL + "add", stock, { withCredentials: true });
  } catch (error) {
    console.error("Error adding stock by ID:", error);
    return null;
  }
};


export const updateStock = async (stock: StockDto) => {
  try {
    return axios.put(API_URL + "update", stock, { withCredentials: true });
  } catch (error) {
    console.error("Error updating stock by ID:", error);
    return null;
  }
}