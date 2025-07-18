import {
    Autocomplete,
    Card,
    CardContent,
    CircularProgress,
    TextField,
    Typography
} from "@mui/material";
import {LinePlot, MarkPlot} from "@mui/x-charts/LineChart";
import {useEffect, useState} from "react";
import dayjs from "dayjs";
import {ChartsLegend, ChartsXAxis, ChartsYAxis, ResponsiveChartContainer} from "@mui/x-charts";
import { PriceHistory, PriceHistoryWithProductId, Product } from "../model/product";
import { getPriceHistory } from "../requests/ProductRequests";

interface PriceHistoryProductCardProps {
    products: Product[] | null;
}


export const PriceHistoryProductCard = ({products}: PriceHistoryProductCardProps) => {
    const [allPriceHistories, setAllPriceHistories] = useState<PriceHistoryWithProductId[]>([]);
    const [finished, setFinished] = useState<boolean>(false);
    const [priceHistoryTimePeriod, setPriceHistoryTimePeriod] = useState<Date[]>([]);
    const [priceHistoryPrices, setPriceHistoryPrices] = useState<number[]>([]);
    const [priceHistoryCostPrices, setPriceHistoryCostPrices] = useState<number[]>([]);
    const [priceHistoryActiveFlags, setPriceHistoryActiveFlags] = useState<boolean[]>([]);
    const [selectedProduct, setSelectedProduct] = useState<Product | null>(products === null ? null : products[0]);

    useEffect(() => {
        if (products?.length) {
            const res = getPriceHistory(products[0].productId);
            res.then((val) => {
                if (val == null) return;
                const body: PriceHistory[] = val.data;
                mapPriceHistory(body);
                setFinished(true);
                setSelectedProduct(products[0]);
                setAllPriceHistories([{priceHistory: body, productId: products[0].productId}]);
            });
        }
    }, [products]);


    const mapPriceHistory = (priceHistory: PriceHistory[]) => {
        const timePeriods: Date[] = [];
        const prices: number[] = [];
        const costPrices: number[] = [];
        const activeFlags: boolean[] = [];

        for (const ph of priceHistory) {
            timePeriods.push(new Date(ph.changedDate));
            prices.push(Number(ph.listPrice));
            costPrices.push(Number(ph.costPrice));
            activeFlags.push(ph.active);
        }

        setPriceHistoryTimePeriod(timePeriods);
        setPriceHistoryPrices(prices);
        setPriceHistoryCostPrices(costPrices);
        setPriceHistoryActiveFlags(activeFlags);
    };


    const handleProductChange = (newValue: Product | null) => {
        if (!newValue) return;
        const res = getPriceHistory(newValue.productId);
        res.then((val) => {
            if (val == null) return;
            const body: PriceHistory[] = val.data;
            mapPriceHistory(body);
        })

        setSelectedProduct(newValue);
    }


    return (
        <div>
            <Card sx={{height: '48vh', width: '100%', display: 'flex', flexDirection: 'column'}}>
                <CardContent sx={{flexGrow: 1}}>
                    <Typography variant="h5" component="div">
                        Preisveränderungen
                    </Typography>
                    <Typography sx={{color: 'text.secondary', mb: 1.5}}>
                        Preisveränderung eines Produktes über die gesamte Zeit
                    </Typography>
                    <Autocomplete
                    className="product-select"
                        options={products || []}
                        getOptionLabel={(option) => option.productName}
                        value={selectedProduct} // Set default value
                        onChange={(_, newValue) => handleProductChange(newValue)}
                        renderInput={(params) => <TextField {...params} label="Produkt wählen"/>}
                    />
                    {finished ? <ResponsiveChartContainer
                            xAxis={[{
                                label: "Daten",
                                data: priceHistoryTimePeriod,
                                tickInterval: priceHistoryTimePeriod,
                                scaleType: "time",
                                valueFormatter: (date) => dayjs(date).format("DD.MM.YY HH:mm")
                            }]}
                            yAxis={[{
                                label: "Preis",
                                min: 0
                            }]}
                            series={[
                                {type: "line", label: "Einkauspreis", data: priceHistoryPrices, showMark: true},
                                {type: "line", label: "Verkaufspreis", data: priceHistoryCostPrices, showMark: true}

                            ]}
                            height={300}
                        >
                            <LinePlot/>
                            <ChartsXAxis/>
                            <ChartsYAxis/>
                            <MarkPlot/>
                            <ChartsLegend/>
                        </ResponsiveChartContainer> :
                        <div>
                            <Typography variant="body2" sx={{mt: 2, color: 'text.secondary'}}>
                                Preisdaten werden geladen...
                            </Typography>
                            <CircularProgress/>
                        </div>
                    }

                </CardContent>
            </Card>
        </div>
    );
};