import axios from 'axios';
import { API_BASE_URL } from "./baseUrl";
import { ProductDto, RespBestSellingProductDto } from '../model/product';

const API_URL = `${API_BASE_URL}/products/`;


/**
 * * Fetches all products from the API.
 */
export const getAllProducts = async () => {
    try {
        return axios.get(API_URL + "all", {withCredentials: true});
    } catch (error) {
        console.error('Error fetching products:', error);
        return null;
    }
}

/**
 * get price history to specific product
 * @param productId
 */
export const getPriceHistory = async (productId: string) => {
    try {
        return axios.get(API_URL + "price-history/" + productId, {withCredentials: true});
    } catch (error) {
        console.error('Error fetching products:', error);
        return null;
    }
}

/**
 * send updated product to backend
 * @param product
 */
export const updateProduct = async (product: ProductDto) => {
    try {
        return await axios.put(API_URL + "update", product, {withCredentials: true});
    } catch (error) {
        console.error('Error updating product:', error);
        return null;
    }
}


/**
 * adding new product
 * @param product
 */
export const addProduct = async (product: ProductDto) => {

    try {
        return axios.post(API_URL + "add", product, {withCredentials: true});
    } catch (error) {
        console.error('Error adding product:', error);
        return null;
    }
}


/**
 * delete product
 * @param productId
 */
export const deleteProduct = async (productId: string) => {
    try {
        return axios.delete(API_URL + "delete/" + productId, {withCredentials: true});
    } catch (error) {
        console.error('Error deleting product:', error);
        return null;
    }
}


/**
 * get all sells of a product
 * @param productId
 */
export const getBestSoldProduct = async (startDate: Date, endDate: Date): Promise<RespBestSellingProductDto | null> => {
  try {
    const params = {
      start: startDate.toISOString(),
      end: endDate.toISOString(),
    };
    const response = await axios.get(`${API_URL}best-selling`, { params, withCredentials: true });
    return response.data;
  } catch (error) {
    console.error("Error fetching best sold product:", error);
    return null;
  }
};
