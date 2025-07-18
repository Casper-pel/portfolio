import {
    Autocomplete, Button,
    Card,
    CardContent, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle,
    FormControlLabel,
    MenuItem,
    Switch,
    TextField,
    Typography,
    useTheme
} from "@mui/material";
import { useEffect, useState } from "react";
import Grid from "@mui/material/Grid";
import { useMediaQuery } from "@mui/system";
import { toast, ToastContainer } from "react-toastify";
import { Product, ProductDto } from "../model/product";
import { addStock, getStockById, updateStock } from "../requests/StockRequests";
import { StockDto } from "../model/stock";
import { deleteProduct, updateProduct } from "../requests/ProductRequests";
import { useAuth } from "@/app/context/AuthContext";


interface ProductUpdateProps {
    allProducts: Product[] | null;
    reloadProducts: () => void; // function to reload products after update
}


export const ProductUpdate = ({ allProducts, reloadProducts }: ProductUpdateProps) => {
    const { accessRights } = useAuth();
    const [products, setProducts] = useState<Product[] | null>(null);
    const [viewAllProducts, setViewAllProducts] = useState<boolean>(false);
    const [noStockAvailable, setNoStockAvailable] = useState<boolean>(false);
    const [changed, setChanged] = useState<boolean>(false);
    const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
    const [stock, setStock] = useState<number | null>(null);
    const [formData, setFormData] = useState<Partial<Product>>({});
    const [open, setOpen] = useState(false);
    const theme = useTheme();
    const fullScreen = useMediaQuery(theme.breakpoints.down('md'));

    useEffect(() => {
        if (allProducts) {
            setProducts(allProducts);
        }
    }, [allProducts]);


    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    useEffect(() => {
        if (!selectedProduct) return;
        const res = getStockById(selectedProduct.productId);
        res.then((value) => {
            if (!value) return;
            const body: StockDto = value.data;
            if (body.quantity === null || body.quantity === undefined) {
                setNoStockAvailable(true);
                setStock(0);
            } else {
                setStock(body.quantity);
            }
        })
    }, [selectedProduct])


    const handleProductChange = (newValue: Product | null) => {
        if (newValue === null || newValue === undefined) return;
        const tmpValue: Product = { ...newValue };
        tmpValue.currency = newValue.currency.toUpperCase();
        console.log("curr", tmpValue.currency);
        setSelectedProduct(tmpValue);
        if (tmpValue) {
            setFormData({ ...tmpValue });
        }
    };

    /**
     * elegantly handle all changes in one method
     * @param field which field in products has changed
     * @param value the new value
     */
    const handleChange = (field: keyof Product, value: string | number | Date | boolean) => {
        setChanged(true);
        setFormData((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    /**
     * handle the change of the stock
     * stocks are stored not at stripe but locally in a db, so it must be updated separately
     * @param value
     */
    const handleStockChange = (value: string) => {
        const val: number = parseInt(value);
        setStock(val);
        setChanged(true);
    }


    /**
     * send changes to backend
     */
    const saveChanges = async () => {
        if (!changed) return;

        const updatedProduct: ProductDto = {
            productName: formData.productName ?? "",
            productId: selectedProduct?.productId ?? "",
            productDescription: formData.productDescription ?? "",
            listPrice: Number(formData.listPrice), // hier wird gecastet
            costPrice: Number(formData.costPrice), // hier auch
            upcCode: formData.upcCode ?? "",
            created: Math.floor(Date.now() / 1000),
            updated: Math.floor(Date.now() / 1000),
            active: formData.active ?? false,
            currency: formData.currency ?? "",
            taxIncludedInPrice: formData.taxIncludedInPrice ?? false,
            priceId: selectedProduct?.priceId ?? ""
        };


        const res = await updateProduct(updatedProduct);
        if (res === null) {
            toast("Fehler beim Speichern der Änderungen", { type: "error" });
        }
        if (noStockAvailable) {
            if (stock === null || stock < 0) return;
            const newStock: StockDto = {
                productId: selectedProduct?.productId ?? "",
                quantity: stock ?? 0,
                repurchased: false,
                shouldBeRepurchased: true
            }
            const resStock = await addStock(newStock);
            if (resStock === null) {
                toast("Fehler beim Hinzufügen des Lagerbestandes", { type: "error" });
            }


        } else {
            debugger;
            const stockD: StockDto = {
                productId: selectedProduct?.productId ?? "",
                quantity: stock ?? 0,
                repurchased: false,
                shouldBeRepurchased: true
            }

            const resStock = await updateStock(stockD);
            if (resStock === null) {
                toast("Fehler beim Speichern des Lagerbestandes", { type: "error" });
            }
        }


        handleProductUpdateClose();
        reloadProducts();
        console.log("changes saved");
    }

    /**
     * delete the selected product
     */
    const handleDelete = async () => {
        console.log("delete", selectedProduct?.productId);
        if (!selectedProduct) return;
        if (!selectedProduct.productId) return;
        const res = await deleteProduct(selectedProduct.productId);
        if (res === null) {
            toast("Fehler beim Löschen des Produkts", { type: "error" });
            console.log("error deleting product");
            return;
        }

        handleClose();
        handleProductUpdateClose();
    }


    const handleProductUpdateClose = () => {
        setSelectedProduct(null);
    }


    const handleOnlyActiveProducts = (onlyActive: boolean) => {
        setViewAllProducts(onlyActive);
        handleProductUpdateClose();
        if (onlyActive) {
            if (!allProducts) return;
            const activeProducts = allProducts.filter((product) => product.active);
            setProducts(activeProducts);
        } else {
            setProducts(allProducts);
        }
    }


    return (
        <div>
            <Card sx={{ height: '48vh' }}>
                <CardContent>
                    <Typography variant="h5" component="div" style={{ marginBottom: "1rem" }}>
                        Produkt updaten
                        <FormControlLabel style={{ marginLeft: "0.25rem" }} control={<Switch checked={viewAllProducts}
                            onChange={(event) => {
                                handleOnlyActiveProducts(event.target.checked);
                            }} />} label="Nur aktive Produkte" />
                    </Typography>

                    <Autocomplete
                        className="product-update-autocomplete"
                        options={products || []}
                        getOptionLabel={(option) => option.productName}
                        isOptionEqualToValue={(option, value) => option.productId === value.productId}
                        value={selectedProduct}
                        onChange={(_, newValue) => handleProductChange(newValue)}
                        renderInput={(params) => (
                            <TextField {...params} label="Produkt wählen" fullWidth />
                        )}
                        sx={{ mb: 3 }}
                    />

                    {selectedProduct && (
                        <Grid container spacing={2}>
                            {/* Column 1 */}
                            <Grid size={4}>

                                <TextField
                                    className="update-product-name"
                                    label="Produktname"
                                    value={formData.productName ?? ""}
                                    onChange={(e) => handleChange("productName", e.target.value)}
                                    fullWidth
                                />
                                <TextField
                                    className="update-product-description"
                                    label="Beschreibung"
                                    value={formData.productDescription ?? ""}
                                    onChange={(e) => handleChange("productDescription", e.target.value)}
                                    fullWidth
                                    sx={{ mt: 2 }}
                                />
                                <p />
                                <br />
                                <FormControlLabel style={{ marginLeft: "2%" }}
                                    className="update-product-label"
                                    control={
                                        <Switch
                                            checked={formData.taxIncludedInPrice ?? false}
                                            onChange={(e) => handleChange("taxIncludedInPrice", e.target.checked)}
                                        />
                                    }
                                    label="Preis inkl. MwSt."
                                />
                            </Grid>

                            {/* Column 2 */}
                            <Grid size={4}>
                                <TextField
                                    className="update-product-list-price"
                                    label="Verkaufspreis"
                                    type="number"
                                    inputProps={{ step: "0.01", min: "0" }}
                                    value={formData.listPrice ?? ""}
                                    onChange={(e) => handleChange("listPrice", e.target.value)}
                                    fullWidth
                                />
                                <TextField
                                    className="update-product-cost-price"
                                    label="Einkaufspreis"
                                    type="number"
                                    inputProps={{ step: "0.01", min: "0" }}
                                    value={formData.costPrice ?? ""}
                                    onChange={(e) => handleChange("costPrice", e.target.value)}
                                    fullWidth
                                    sx={{ mt: 2 }}
                                />

                                <TextField
                                    className="update-product-stock"
                                    label="Lagerbestand"
                                    value={stock ?? 0}
                                    type="number"
                                    onChange={(e) => handleStockChange(e.target.value)}
                                    fullWidth
                                    sx={{ mt: 2 }}
                                />
                            </Grid>

                            {/* Column 3 */}
                            <Grid size={4}>

                                <TextField
                                    className="update-product-currency"
                                    label="Währung"
                                    select
                                    value={formData.currency ?? ""}
                                    onChange={(e) => handleChange("currency", e.target.value)}
                                    fullWidth
                                >
                                    <MenuItem value="eur">EUR</MenuItem>
                                    <MenuItem value="usd">USD</MenuItem>
                                </TextField>
                                <TextField
                                    className="update-product-upc-code"
                                    label="UPC Code"
                                    value={formData.upcCode ?? ""}
                                    onChange={(e) => handleChange("upcCode", e.target.value)}
                                    fullWidth
                                    sx={{ mt: 2 }}
                                />

                                <FormControlLabel
                                    className="update-product-active"
                                    control={
                                        <Switch
                                            checked={formData.active ?? false}
                                            onChange={(e) => handleChange("active", e.target.checked)}
                                        />
                                    }
                                    label="Aktiv"
                                    sx={{ mt: 2 }}

                                />


                            </Grid>
                            {(accessRights.includes("product.update") || accessRights.includes("admin")) && changed ?
                                <Button variant={"contained"} onClick={saveChanges} className="update-product-save">Speichern</Button>
                                :
                                <Button variant={"contained"} disabled className="update-product-save-disabled" >Speichern</Button>
                            }
                            {(accessRights.includes("product.delete") || accessRights.includes("admin")) && (
                                <Button variant={"contained"} color={"error"} onClick={handleClickOpen} className="update-product-delete">Löschen</Button>
                            )}
                            <Button variant={"outlined"} onClick={handleProductUpdateClose} className="update-product-close">
                                schließen
                            </Button>
                            <Dialog
                                fullScreen={fullScreen}
                                open={open}
                                onClose={handleClose}
                                aria-labelledby="responsive-dialog-title"
                            >
                                <DialogTitle id="responsive-dialog-title">
                                    {"Produkt wirklich löschen?"}
                                </DialogTitle>
                                <DialogContent>
                                    <DialogContentText>
                                        Wenn Sie das Produkt löschen, wird es nicht mehr zur Verfügugn stehen.
                                        Dazugehörige Lagerbestände werden gelöscht
                                    </DialogContentText>
                                </DialogContent>
                                <DialogActions>
                                    <Button autoFocus onClick={handleClose}>
                                        Abbrechen
                                    </Button>
                                    <Button onClick={handleDelete} autoFocus color={"error"}>
                                        Löschen
                                    </Button>
                                </DialogActions>
                            </Dialog>
                        </Grid>
                    )}
                </CardContent>
            </Card>
            <ToastContainer />
        </div>
    );
};