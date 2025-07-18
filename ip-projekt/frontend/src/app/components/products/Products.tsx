"use client";
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import { useEffect, useState } from "react";
import { Product, ProductDto } from '../model/product';
import { getAllProducts } from '../requests/ProductRequests';
import { ProductGraph } from './ProductGraph';
import { BestSoldProductCard } from './BestSoldProductCard';
import { PriceHistoryProductCard } from './PriceHistoryProductCard';
import { ProductUpdate } from './ProductUpdate';
import { NewProduct } from './NewProduct';
import { useAuth } from '@/app/context/AuthContext';
import { Typography } from '@mui/material';

/**
 * Products component
 * In here everything comes together and is displayed
 */
export const Products = () => {
    const { user, accessRights, isLoading, isAuthenticated } = useAuth();

    const [products, setProducts] = useState<Product[] | null>(null);

    // fetch all products from the api, to have them ready and not request every time
    useEffect(() => {
        const resp = getAllProducts();
        resp.then((response) => {
            if (response && response.status === 200) {
                mapProductDtos(response.data);
            } else {
                console.error("Error fetching products");
            }
        }).catch((error) => {
            console.error("Error fetching products", error);
        });
    }, []);

    // maps the product dtos to products, cause stripe products are using unix timestamps
    const mapProductDtos = (productDtos: ProductDto[]) => {
        const productsArr: Product[] = [];
        for (const productDto of productDtos) {
            const product: Product = {
                ...productDto,
                created: new Date(productDto.created * 1000),
                updated: new Date(productDto.updated * 1000),
            };
            productsArr.push(product);
        }
        setProducts(productsArr);
    };



    const reloadProducts = () => {
        const resp = getAllProducts();
        resp.then((response) => {
            if (response && response.status === 200) {
                mapProductDtos(response.data);
            } else {
                console.error("Error fetching products");
            }
        }).catch((error) => {
            console.error("Error fetching products", error);
        });
    }

    if (isLoading) {
        return (
            <Box sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: '100vh'
            }}>
                <Typography variant="h6">Laden...</Typography>
            </Box>
        );
    }

    return (
        <Box sx={{
            height: '100vh',
            display: 'flex',
            flexDirection: 'column',
            px: 3,
            py: 2,
            overflow: 'hidden'
        }}>
            <Box sx={{
                flexGrow: 1,
                display: 'flex',
                flexDirection: 'column',
                minHeight: 0
            }}>
                {/* First row */}
                <Box sx={{ flex: '1 1 50%', mb: 2, minHeight: 0 }}>
                    <Grid container spacing={2} sx={{ height: '100%' }}>
                        <Grid size={4} sx={{ height: '100%' }}>
                            <Box sx={{ height: '100%' }}>
                                <ProductGraph products={products} />
                            </Box>
                        </Grid>
                        <Grid size={4} sx={{ height: '100%' }}>
                            <Box sx={{ height: '100%' }}>
                                <BestSoldProductCard products={products} />
                            </Box>
                        </Grid>
                        <Grid size={4} sx={{ height: '100%' }}>
                            <Box sx={{ height: '100%' }}>
                                <PriceHistoryProductCard products={products} />
                            </Box>
                        </Grid>
                    </Grid>
                </Box>

                {/* Second row */}
                <Box sx={{ flex: '1 1 50%', minHeight: 0 }}>
                    <Grid container spacing={2} sx={{ height: '100%' }}>
                        {accessRights && (accessRights.includes("product.update") || accessRights.includes("admin") || accessRights.includes("product.delete")) && (
                            <Grid size={6} sx={{ height: '100%' }}>
                                <Box sx={{ height: '100%' }}>
                                    <ProductUpdate allProducts={products} reloadProducts={reloadProducts} />
                                </Box>
                            </Grid>
                        )}
                        {accessRights && (accessRights.includes("product.create") || accessRights.includes("admin")) && (
                            <Grid size={6} sx={{ height: '100%' }}>
                                <Box sx={{ height: '100%' }}>
                                    <NewProduct reloadProducts={reloadProducts} />
                                </Box>
                            </Grid>
                        )}
                    </Grid>
                </Box>
            </Box>
        </Box>
    );
};