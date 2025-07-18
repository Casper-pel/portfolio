import { Button, Card, CardContent, FormControlLabel, MenuItem, Switch, TextField, Typography } from "@mui/material";
import Grid from "@mui/material/Grid";
import React, { useState } from "react";
import { StockDto } from "../model/stock";
import { addStock } from "../requests/StockRequests";
import { toast } from "react-toastify";
import { Product, ProductDto } from "../model/product";
import { addProduct } from "../requests/ProductRequests";

interface NewProductProps {
    reloadProducts: () => void;
}


export const NewProduct = ({reloadProducts}: NewProductProps) => {
    const [initStock, setInitStock] = useState<number>(0);
    const [formData, setFormData] = useState<Partial<Product>>({});

    const handleChange = (field: keyof Product, value: number | Date | string | boolean) => {
        console.log("changes: ", value);
        setFormData((prev) => ({
            ...prev,
            [field]: value,
        }));
    };


    /**
     * method to store
     */
    const handleCreate = () => {
        console.log(formData);
        if (!validateData()) return;
        if (formData.productName === null || formData.productName === undefined) return;
        if (formData.productDescription === null || formData.productDescription === undefined) return;
        if (formData.upcCode === null || formData.upcCode === undefined) return;
        if (formData.listPrice === null || formData.listPrice === undefined) return;
        if (formData.currency === null || formData.currency === undefined) return;
        if (formData.taxIncludedInPrice === null || formData.taxIncludedInPrice === undefined) return;
        if (formData.active === null || formData.active === undefined) return;
        const newProduct: ProductDto = {
            productName: formData.productName,
            productId: "",
            productDescription: formData.productDescription,
            listPrice: Number(formData.listPrice),
            costPrice: Number(formData.costPrice),
            upcCode: formData.upcCode,
            created: Math.floor(Date.now() / 1000),
            updated: Math.floor(Date.now() / 1000),
            active: formData.active,
            currency: formData.currency,
            taxIncludedInPrice: formData.taxIncludedInPrice,
            priceId: "" 
        }
        console.log(newProduct);
        const res = addProduct(newProduct);
        res.then((ret) => {
            if (ret === null) return;
            if (ret.status === 200) {
                const prodId = ret.data;
                console.log("Product created with ID: ", prodId);
                handleStockCreate(prodId);
                reloadProducts();
            } else {
                toast.error("Fehler beim Erstellen des Produkts");
            }
        }
        )
    }

    /**
     * handles the creation of the stock for a product
     * @param prodId the product id of the product that was just created
     */
    const handleStockCreate = (prodId: string) => {
        const stockData: StockDto = {
            productId: prodId,
            quantity: initStock,
            repurchased: false,
            shouldBeRepurchased: true
        }

        const res = addStock(stockData);
        res.then((ret) => {
            if (ret === null) return;
            if (ret.status === 200) {
                toast.success("Produkt erfolgreich erstellt!");
                setFormData(() => ({}))
                setInitStock(0);
            } else {
                alert(ret.data);
            }
        });
    }


    /**
     * validate the input from the user
     */
    const validateData = (): boolean => {
        if (!formData.productName || formData.productName.length === 0) {
            console.log("Fehler: Produktname fehlt");
            return false;
        }
        if (!formData.productDescription || formData.productDescription.length === 0) {
            console.log("Fehler: Produktbeschreibung fehlt");
            return false;
        }
        if (!formData.upcCode || formData.upcCode.length === 0) {
            console.log("Fehler: UPC fehlt");
            return false;
        }
        if (!formData.listPrice || Number(formData.listPrice) <= 0) {
            console.log("Fehler: Verkaufspreis fehlt oder <= 0");
            return false;
        }
        if (!formData.costPrice || Number(formData.costPrice) <= 0) {
            console.log("Fehler: Einkaufspreis fehlt oder <= 0");
            return false;
        }
        if (Number(formData.listPrice) < Number(formData.costPrice)) {
            console.log("Fehler: Verkaufspreis < Einkaufspreis");
            return false;
        }
        if (!formData.currency || formData.currency.length === 0) {
            console.log("Fehler: Währung fehlt");
            return false;
        }
        return true;
    }



    return (
        <div>
            <Card sx={{ height: '48vh' }}>
                <CardContent>
                    <Typography variant="h5" component="div" style={{ marginBottom: "1rem" }}>
                        Produkt erstellen
                    </Typography>
                    <Grid container spacing={2}>
                        {/*Row 1*/}
                        <Grid size={4}>
                            <TextField
                                className="new-product-name"
                                label="Produktname"
                                value={formData.productName ?? ""}
                                onChange={(e) => handleChange("productName", e.target.value)}
                                fullWidth
                            />
                            <TextField
                                className="new-product-price"
                                label="Verkaufspreis"
                                value={formData.listPrice ?? ""}
                                type={"number"}
                                onChange={(e) => handleChange("listPrice", e.target.value)}
                                fullWidth
                                sx={{ mt: 2 }}
                            />
                            <FormControlLabel style={{ marginLeft: "2%" }}
                                control={
                                    <Switch
                                        checked={formData.taxIncludedInPrice ?? false}
                                        onChange={(e) => handleChange("taxIncludedInPrice", e.target.checked)}
                                    />
                                }
                                label="Preis inkl. MwSt."
                                sx={{ mt: 2 }}
                                className="new-product-tax-included"
                            />
                        </Grid>


                        {/*Row 2*/}
                        <Grid size={4}>
                            <TextField
                                className="new-product-description"
                                label="Produktbeschreibung"
                                value={formData.productDescription ?? ""}
                                onChange={(e) => handleChange("productDescription", e.target.value)}
                                fullWidth
                            />
                            <TextField
                                className="new-product-cost-price"
                                label="Einkaufspreis"
                                value={formData.costPrice ?? ""}
                                type={"number"}
                                onChange={(e) => handleChange("costPrice", e.target.value)}
                                fullWidth
                                sx={{ mt: 2 }}
                            />
                            <FormControlLabel
                                className="new-product-active"
                                control={
                                    <Switch
                                        checked={formData.active ?? false}
                                        onChange={(e) => handleChange("active", e.target.checked)}
                                    />
                                }
                                label="Aktiv"
                                sx={{ ml: 1, mt: 2 }}

                            />
                        </Grid>

                        {/*Row 3*/}
                        <Grid size={4}>
                            <TextField
                                className="new-product-upc-code"
                                label="UpcCode"
                                value={formData.upcCode ?? ""}
                                type={"number"}
                                onChange={(e) => handleChange("upcCode", e.target.value)}
                                fullWidth
                            />
                            <TextField
                                className="new-product-currency"
                                label="Währung"
                                select
                                value={formData.currency ?? ""}
                                onChange={(e) => handleChange("currency", e.target.value)}
                                fullWidth
                                sx={{ mt: 2 }}
                            >
                                <MenuItem value="EUR">EUR</MenuItem>
                                <MenuItem value="USD">USD</MenuItem>
                                <MenuItem value="CHF">CHF</MenuItem>
                            </TextField>
                            <p />
                            <br />
                            <TextField
                                className="new-product-init-stock"
                                label="Initialer Lagerbestand"
                                value={initStock || ''}
                                type="number"
                                onChange={(e) => {
                                    const value = e.target.value;
                                    const number = Number(value);
                                    if (value === '') {
                                        setInitStock(0); // oder leer lassen je nach UX
                                    } else if (number >= 0) {
                                        setInitStock(number);
                                    }
                                }}
                                fullWidth
                            />
                        </Grid>
                        <Button variant={"contained"} onClick={handleCreate} sx={{ mt: 2 }} fullWidth>
                            erstellen
                        </Button>
                    </Grid>
                </CardContent>
            </Card>
        </div>
    )
}