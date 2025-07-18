import { Autocomplete, Card, CardContent, TextField, Typography } from "@mui/material";
import { LineChart } from "@mui/x-charts/LineChart";
import { useEffect, useState } from "react";
import { getBestSoldProduct } from "../requests/ProductRequests";
import { Product, RespBestSellingProductDto } from "../model/product";

interface BestSoldProductCardProps {
    products: Product[] | null;
}

export const BestSoldProductCard = ({ products }: BestSoldProductCardProps) => {
    const timeOptions = ["letzte Woche", "letzter Monat", "letztes Jahr", "letztes Geschäftsjahr", "Allzeit"];
    const [selectedTimeOption, setSelectedTimeOption] = useState<number>(0);
    const [selectedProduct, setSelectedProduct] = useState<RespBestSellingProductDto | null>(null);
    const [sells, setSells] = useState<number>(0);
    const [allSellsTimePeriod, setAllSellsTimePeriod] = useState<string[]>([]);
    const [allSells, setAllSells] = useState<number[]>([]);

    // Fetches the best sold product for a specific time range
    const fetchDataForTimeRange = (start: Date, end: Date) => {
        getBestSoldProduct(start, end)
            .then((response) => {
                if (response && response.dates && response.dates.length > 0) {
                    setSelectedProduct(response);
                    setSells(response.totalQuantity);
                    const countsAndDates = processSalesDates(response.dates, response.totalQuantity);
                    setAllSells(countsAndDates.counts);
                    setAllSellsTimePeriod(countsAndDates.dates);
                } else {
                    setSelectedProduct(null);
                    setSells(0);
                    setAllSells([]);
                    setAllSellsTimePeriod([]);
                }
            })
            .catch((error) => {
                console.error("Error fetching best sold product", error);
                setSelectedProduct(null);
                setSells(0);
                setAllSells([]);
                setAllSellsTimePeriod([]);
            });
    };

    // Handles selection of a time range from dropdown
    const handleTimeSelect = (index: number) => {
        setSelectedTimeOption(index);
        const now = new Date();
        let start: Date;

        switch (index) {
            case 0: // last week
                start = new Date();
                start.setDate(now.getDate() - 7);
                break;
            case 1: // last month
                start = new Date();
                start.setMonth(now.getMonth() - 1);
                break;
            case 2: // last year
                start = new Date();
                start.setFullYear(now.getFullYear() - 1);
                break;
            case 3: // last fiscal year
                start = new Date(now.getFullYear() - 1, 0, 1);
                now.setFullYear(now.getFullYear() - 1);
                now.setMonth(11);
                now.setDate(31);
                break;
            case 4: // all time
                start = new Date(2000, 0, 1);
                break;
            default:
                start = new Date();
        }

        fetchDataForTimeRange(start, new Date());
    };

    // Runs once on component mount to fetch initial data
    useEffect(() => {
        handleTimeSelect(selectedTimeOption);
    }, []);

    // Processes the array of sale dates and returns sorted counts per date
    const processSalesDates = (dates: string[], totalQuantity: number): { dates: string[], counts: number[] } => {
        const dateCountMap = new Map<string, number>();

        dates.forEach(dateString => {
            const date = new Date(dateString);
            const dateKey = date.toISOString().split('T')[0]; // yyyy-mm-dd
            dateCountMap.set(dateKey, (dateCountMap.get(dateKey) || 0) + 1);
        });

        const sortedEntries = Array.from(dateCountMap.entries())
            .sort(([a], [b]) => new Date(a).getTime() - new Date(b).getTime());

        // If there is only one sale day and totalQuantity is higher than count, use totalQuantity directly
        if (sortedEntries.length === 1 && totalQuantity > sortedEntries[0][1]) {
            return {
                dates: sortedEntries.map(([date]) => date),
                counts: [totalQuantity]
            };
        }

        return {
            dates: sortedEntries.map(([date]) => date),
            counts: sortedEntries.map(([, count]) => count)
        };
    };

    return (
        <div>
            <Card style={{ height: "48vh", display: "flex", flexDirection: "column" }}>
                <CardContent>
                    <Typography variant="h5" component="div" style={{ marginBottom: "1rem" }}>
                        Am besten verkauftes Produkt
                    </Typography>

                    <Autocomplete
                        className="bestsold-select"
                        options={timeOptions}
                        value={timeOptions[selectedTimeOption]}
                        onChange={(_, newValue) => {
                            const index = timeOptions.indexOf(newValue || "");
                            if (index !== -1) {
                                handleTimeSelect(index);
                            }
                        }}
                        renderInput={(params) => <TextField {...params} label="Zeitraum wählen" />}
                        sx={{ mb: 2 }}
                    />

                    <Typography variant="h5" component="div">
                        Produktname: {selectedProduct?.productName || "—"}
                    </Typography>

                    <Typography sx={{ color: "text.secondary", mb: 1.5 }}>
                        Verkäufe in {timeOptions[selectedTimeOption]}: {selectedProduct?.totalQuantity ?? 0}
                    </Typography>

                    <LineChart
                        xAxis={[{ data: allSellsTimePeriod, scaleType: "point" }]}
                        series={[{ data: allSells }]}
                        width={500}
                        height={250}
                    />
                </CardContent>
            </Card>
        </div>
    );
};
