import { useEffect, useState } from "react";
import { CouponDto } from "../model/coupon";
import { deleteCoupon, getAllCoupons } from "../requests/CouponRequests";
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Button,
    Box,
    Typography,
} from "@mui/material";
import { useAuth } from "@/app/context/AuthContext";
import NewCouponDialog from "./NewCouponDialog";

export const CouponView = () => {
    const { accessRights } = useAuth();
    const [allCoupons, setAllCoupons] = useState<CouponDto[]>([]);
    const [dialogOpen, setDialogOpen] = useState(false);

    useEffect(() => {
        if (
            accessRights.length > 0 &&
            !accessRights.includes("coupons.read") &&
            !accessRights.includes("admin")
        ) {
            window.location.href = "/dashboard";
        }
    }, [accessRights]);

    useEffect(() => {
        getAllCoupons()
            .then((response) => {
                if (response && response.status !== 403) {
                    setAllCoupons(response.data);
                } else {
                    console.error("Failed to fetch coupons");
                }
            })
            .catch((error) => {
                console.error("Error fetching coupons:", error);
            });
    }, []);

    const handleDelete = async (name: string) => {
        const ret = await deleteCoupon(name);
        if (ret?.status === 200) {
            setAllCoupons(allCoupons.filter((coupon) => coupon.name !== name));
        }
    };

    const handleCouponCreated = (open: boolean) => {


        getAllCoupons()            .then((response) => {
                if (response && response.status !== 403) {
                    setAllCoupons(response.data);
                } else {
                    console.error("Failed to fetch coupons after creation");
                }
            })
            .catch((error) => {
                console.error("Error fetching coupons after creation:", error);
            });
        // Close the dialog and refresh the coupon list



        setDialogOpen(open)
    };

    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                p: 3,
                width: "100%",
                maxWidth: 1200,
                mx: "auto",
            }}
        >
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    width: "100%",
                    mb: 3,
                }}
            >
                <Typography variant="h4">Coupon view</Typography>

                {accessRights.includes("coupons.create") || accessRights.includes("admin") && (
                    <Button
                        variant="contained"
                        onClick={() => setDialogOpen(true)}
                    >
                        Neuen Coupon erstellen
                    </Button>
                )}
            </Box>


            <TableContainer component={Paper} sx={{ width: "100%", maxWidth: 1200 }}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Name</TableCell>
                            <TableCell>Amount Off</TableCell>
                            <TableCell>Currency</TableCell>
                            <TableCell>Duration</TableCell>
                            <TableCell>Percent Off</TableCell>
                            {(accessRights.includes("coupons.delete") ||
                                accessRights.includes("coupons.update") ||
                                accessRights.includes("admin")) && <TableCell>Actions</TableCell>}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {allCoupons.length > 0 ? (
                            allCoupons.map((coupon) => (
                                <TableRow key={coupon.id}>
                                    <TableCell>{coupon.id}</TableCell>
                                    <TableCell>{coupon.name}</TableCell>
                                    <TableCell>
                                        {coupon.amountOff > 0 ? coupon.amountOff / 100 : "-"}
                                    </TableCell>
                                    <TableCell>{coupon.currency === "eur" ? "€" : "$"}</TableCell>
                                    <TableCell>{coupon.duration}</TableCell>
                                    <TableCell>
                                        {coupon.percentOff > 0 ? `${coupon.percentOff}%` : "-"}
                                    </TableCell>
                                    {(accessRights.includes("coupons.delete") || accessRights.includes("admin")) && (
                                        <TableCell>
                                            <Button
                                                size="small"
                                                variant="contained"
                                                color="error"
                                                onClick={() => handleDelete(coupon.name)}
                                            >
                                                Löschen
                                            </Button>
                                        </TableCell>
                                    )}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell colSpan={7} align="center">
                                    Keine Coupons vorhanden
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </TableContainer>
        <NewCouponDialog open={dialogOpen} onClose={() => handleCouponCreated(false)} />

        </Box>
        
    );
};
