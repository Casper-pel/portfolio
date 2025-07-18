"use client";
import {Autocomplete, Card, CardContent, TextField, Typography} from "@mui/material";
import {useState, useEffect} from "react";
import { Product } from "../model/product";
import { getStockById } from "../requests/StockRequests";

interface ProductGraphProps {
    products: Product[] | null;
}

/**
 * lists all products in a dropdown to select one and retrieve a graph view of the price development of the product
 * @param products
 */
export const ProductGraph = ({ products }: ProductGraphProps) => {
    const [selectedProduct, setSelectedProduct] = useState<Product | null>(products?.[0] || null);
    const [stock, setStock] = useState<number>(0);


    /**
     * Fetch stock when the component mounts or products change
     */
    useEffect(() => {
        if (products?.length) {
            setSelectedProduct(products[0]);
            requestStockOnProduct(products[0].productId);
        }
    }, [products]);

    /**
     * handle to update the selected product and fetch its stock
     * @param newValue selected product from dropdown
     */
    const handleProductChange = (newValue: Product | null) => {
        if (newValue) {
            requestStockOnProduct(newValue.productId);
        }
        setSelectedProduct(newValue);
    }


    /**
     * fetches the stock of a product by its id
     * @param productId product id of selected product in dropdown
     */
    const requestStockOnProduct = (productId: string) => {
        const resp = getStockById(productId);
        resp.then((response) => {
            if (response && response.status === 200) {
                setStock(response.data.quantity);
            } else {
                console.log("Error fetching stock");
                setStock(0);
            }
        })
    }

    return (
        <div>
            <Card style={{height: "48vh"}}>
                <CardContent>
                    <Typography variant="h5" component="div" style={{marginBottom: "1rem"}}>
                        Produkt Infos
                    </Typography>


                    <Autocomplete
                        className="productgraph-select"
                        options={products || []}
                        getOptionLabel={(option) => option.productName}
                        value={selectedProduct} // Set default value
                        onChange={(_, newValue) => handleProductChange(newValue)}
                        renderInput={(params) => <TextField {...params} label="Produkt wählen" />}
                    />

                    {selectedProduct && (
                        <>
                            <Typography variant="h5" component="div">
                                Produktname: {selectedProduct.productName}
                            </Typography>
                            <Typography sx={{ color: "text.secondary", mb: 1.5 }}>
                                Preis: {selectedProduct.listPrice} {selectedProduct.currency.toUpperCase()}
                            </Typography>
                            <Typography variant="body2">Produkt ID: {selectedProduct.productId}</Typography>
                            <Typography variant="body2">Beschreibung: {selectedProduct.productDescription}</Typography>
                            <Typography variant="body2">Verkaufspreis: {selectedProduct.listPrice}</Typography>
                            <Typography variant="body2">Einkaufspreis: {selectedProduct.costPrice}</Typography>
                            <Typography variant="body2">Lagerbestand: {stock}</Typography>
                            <Typography variant="body2">Währung: {selectedProduct.currency.toUpperCase()}</Typography>
                            <Typography variant="body2">Steuern inbegriffen: {selectedProduct.taxIncludedInPrice ? "Ja" : "Nein"}</Typography>
                            <Typography variant="body2">UPC-Code: {selectedProduct.upcCode}</Typography>


                        </>
                    )}
                </CardContent>

            </Card>
        </div>
    );
};