"use client";

import React, { useState, useEffect } from "react";
import {
  Box,
  MenuItem,
  Select,
  Typography,
  Card,
  CardContent,
  InputLabel,
  FormControl,
  LinearProgress,
  Button,
} from "@mui/material";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { LocalizationProvider, DatePicker } from "@mui/x-date-pickers";
import { Dayjs } from "dayjs";
import { API_BASE_URL } from "../../components/requests/baseUrl";
import { useAuth } from "@/app/context/AuthContext";

const FinanceInsights = () => {
  const { accessRights } = useAuth();

  const [timeRange, setTimeRange] = useState("today");
  const [startDate, setStartDate] = useState<Dayjs | null>(null);
  const [endDate, setEndDate] = useState<Dayjs | null>(null);
  const [employees, setEmployees] = useState<
    { employeeId: number; firstName: string; lastName: string }[]
  >([]);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState<string | number>(
    "all"
  );
  const [orders, setOrders] = useState<any[]>([]);
  const [totalSold, setTotalSold] = useState(0);
  const [transactionCount, setTransactionCount] = useState(0);
  const [avgTransaction, setAvgTransaction] = useState(0);
  const [lastTotalSold, setLastTotalSold] = useState(0);
  const [lastTransactionCount, setLastTransactionCount] = useState(0);
  const [lastAvgTransaction, setLastAvgTransaction] = useState(0);
  const [percentTotalSold, setPercentTotalSold] = useState(0);
  const [percentTransactionCount, setPercentTransactionCount] = useState(0);
  const [percentAvgTransaction, setPercentAvgTransaction] = useState(0);
  const [loadedTimeRange, setLoadedTimeRange] = useState("");
  const [dataLoaded, setDataLoaded] = useState(false);

  // Check access rights
  useEffect(() => {
    if (accessRights.length > 0) {
      if (
        !accessRights.includes("finances") &&
        !accessRights.includes("admin")
      ) {
        window.location.href = "/dashboard";
      }
    }
  }, [accessRights]);

  useEffect(() => {
    fetch(`${API_BASE_URL}/employee/all`, {
      credentials: "include"
    })
      .then((res) => res.json())
      .then((data) => setEmployees(data))
      .catch((err) => console.error("Fehler beim Laden der Mitarbeiter:", err));
  }, []);

  const handleTimeRangeChange = (event: any) => {
    setTimeRange(event.target.value);
    if (event.target.value !== "custom") {
      setStartDate(null);
      setEndDate(null);
    }
  };

  const handleLoadData = async () => {
    let calcStartDate: Date | null = startDate
      ? new Date(startDate.toDate())
      : null;
    let calcEndDate: Date | null = endDate ? new Date(endDate.toDate()) : null;
    const today = new Date();

    if (timeRange === "today") {
      calcStartDate = new Date(today);
      calcStartDate.setHours(0, 0, 0, 0);
      calcEndDate = new Date(today);
      calcEndDate.setHours(23, 59, 59, 999);
    } else if (timeRange === "week") {
      calcEndDate = new Date(today);
      calcEndDate.setHours(23, 59, 59, 999);
      calcStartDate = new Date(today);
      calcStartDate.setDate(today.getDate() - 6);
      calcStartDate.setHours(0, 0, 0, 0);
    } else if (timeRange === "month") {
      calcEndDate = new Date(today);
      calcEndDate.setHours(23, 59, 59, 999);
      calcStartDate = new Date(today);
      calcStartDate.setDate(today.getDate() - 29);
      calcStartDate.setHours(0, 0, 0, 0);
    } else if (timeRange === "custom" && startDate && endDate) {
      calcStartDate = new Date(startDate.toDate());
      calcStartDate.setHours(0, 0, 0, 0);
      calcEndDate = new Date(endDate.toDate());
      calcEndDate.setHours(23, 59, 59, 999);
    }

    if (!calcStartDate || !calcEndDate) {
      console.error("Invalid date range");
      return;
    }

    const startISO = calcStartDate.toISOString();
    const endISO = calcEndDate.toISOString();

    let lastStartDate: Date, lastEndDate: Date;
    if (timeRange === "today") {
      lastStartDate = new Date(calcStartDate);
      lastStartDate.setDate(lastStartDate.getDate() - 1);
      lastStartDate.setHours(0, 0, 0, 0);
      lastEndDate = new Date(lastStartDate);
      lastEndDate.setHours(23, 59, 59, 999);
    } else if (timeRange === "week") {
      lastEndDate = new Date(calcStartDate);
      lastEndDate.setDate(lastEndDate.getDate() - 1);
      lastEndDate.setHours(23, 59, 59, 999);
      lastStartDate = new Date(lastEndDate);
      lastStartDate.setDate(lastStartDate.getDate() - 6);
      lastStartDate.setHours(0, 0, 0, 0);
    } else if (timeRange === "month") {
      lastEndDate = new Date(calcStartDate);
      lastEndDate.setDate(lastEndDate.getDate() - 1);
      lastEndDate.setHours(23, 59, 59, 999);
      lastStartDate = new Date(lastEndDate);
      lastStartDate.setDate(lastStartDate.getDate() - 29);
      lastStartDate.setHours(0, 0, 0, 0);
    } else if (timeRange === "custom" && calcStartDate && calcEndDate) {
      const diffDays =
        Math.ceil(
          (calcEndDate.getTime() - calcStartDate.getTime()) /
          (1000 * 60 * 60 * 24)
        ) || 0;
      lastEndDate = new Date(calcStartDate);
      lastEndDate.setDate(lastEndDate.getDate() - 1);
      lastEndDate.setHours(23, 59, 59, 999);
      lastStartDate = new Date(lastEndDate);
      lastStartDate.setDate(lastStartDate.getDate() - diffDays + 1);
      lastStartDate.setHours(0, 0, 0, 0);
    } else {
      lastStartDate = new Date(calcStartDate);
      lastEndDate = new Date(calcEndDate);
    }

    const lastStartISO = lastStartDate.toISOString();
    const lastEndISO = lastEndDate.toISOString();

    const employeeParam =
      selectedEmployeeId !== "all" ? `&employeeId=${selectedEmployeeId}` : "";

    try {
      const response = await fetch(
        `${API_BASE_URL}/order/between?start=${encodeURIComponent(
          startISO
        )}&end=${encodeURIComponent(endISO)}${employeeParam}`,
        {
          method: "GET",
          credentials: "include",
        }
      );
      if (!response.ok) throw new Error("Fehler beim Laden der Bestellungen");
      const orders = await response.json();
      setOrders(orders);

      const total = orders.reduce(
        (sum: number, order: any) => sum + (order.totalPrice ?? 0),
        0
      );
      const count = orders.length;
      const avg = count > 0 ? total / count : 0;

      setTotalSold(total);
      setTransactionCount(count);
      setAvgTransaction(avg);

      const lastResponse = await fetch(
        `${API_BASE_URL}/order/between?start=${encodeURIComponent(
          lastStartISO
        )}&end=${encodeURIComponent(lastEndISO)}${employeeParam}`,
        {
          method: "GET",
          credentials: "include",
        }
      );
      if (!lastResponse.ok)
        throw new Error("Fehler beim Laden der Vergleichsdaten");
      const lastOrders = await lastResponse.json();

      const lastTotal = lastOrders.reduce(
        (sum: number, order: any) => sum + (order.totalPrice ?? 0),
        0
      );
      const lastCount = lastOrders.length;
      const lastAvg = lastCount > 0 ? lastTotal / lastCount : 0;

      setLastTotalSold(lastTotal);
      setLastTransactionCount(lastCount);
      setLastAvgTransaction(lastAvg);

      setPercentTotalSold(
        lastTotal === 0 ? 0 : ((total - lastTotal) / lastTotal) * 100
      );
      setPercentTransactionCount(
        lastCount === 0 ? 0 : ((count - lastCount) / lastCount) * 100
      );
      setPercentAvgTransaction(
        lastAvg === 0 ? 0 : ((avg - lastAvg) / lastAvg) * 100
      );

      setLoadedTimeRange(timeRange);
      setDataLoaded(true);
    } catch (error) {
      setDataLoaded(false);
      console.error("Fehler beim Laden der Finanzdaten:", error);
    }
  };

  const getPercentLabel = (percent: number, lastValue: number): string => {
    if (timeRange === "custom") return "";
    if (!dataLoaded) return "";
    if (lastValue === 0) return "—";
    const prefix = percent >= 0 ? "+" : "";
    let suffix = "";
    if (loadedTimeRange === "today") suffix = "seit gestern";
    else if (loadedTimeRange === "week") suffix = "seit letzter Woche";
    else suffix = "seit letztem Monat";
    return `${prefix}${percent.toFixed(1)}% ${suffix}`;
  };

  return (
    <Box
      p={3}
      display="flex"
      flexDirection="column"
      marginTop="10vh"
      alignItems="center"
      height="70vh"
    >
      <Typography variant="h4" gutterBottom mb={3}>
        Finanzübersicht
      </Typography>

      {/* Filters */}
      <Box
        sx={{
          display: "flex",
          gap: 2,
          mb: 3,
          flexWrap: "wrap",
          justifyContent: "center",
        }}
      >
        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel>Zeitspanne</InputLabel>
          <Select
            className="time-range"
            label="Zeitspanne"
            value={timeRange}
            onChange={handleTimeRangeChange}
          >
            <MenuItem value="today">Heute</MenuItem>
            <MenuItem value="week">Letzten 7 Tagen</MenuItem>
            <MenuItem value="month">Letzen 30 Tagen</MenuItem>
            <MenuItem value="custom">Benutzerdefiniert</MenuItem>
          </Select>
        </FormControl>

        {timeRange === "custom" && (
          <>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <DatePicker
                className="start-date"
                label="Start"
                value={startDate}
                format="DD/MM/YYYY"
                onChange={(newValue) => setStartDate(newValue)}
                sx={{ minWidth: 150 }}
              />
            </LocalizationProvider>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <DatePicker
              className="end-date"
                label="Ende"
                value={endDate}
                format="DD/MM/YYYY"
                onChange={(newValue) => setEndDate(newValue)}
                sx={{ minWidth: 150 }}
              />
            </LocalizationProvider>
          </>
        )}

        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel>Mitarbeiter</InputLabel>
          <Select
            label="Mitarbeiter"
            value={selectedEmployeeId}
            onChange={(e) => {
              const value = e.target.value;
              setSelectedEmployeeId(value === "all" ? "all" : Number(value));
            }}
          >
            <MenuItem value="all">Alle Mitarbeiter</MenuItem>
            {employees.map((employee) => (
              <MenuItem key={employee.employeeId} value={employee.employeeId}>
                {employee.firstName + " " + employee.lastName}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <Button
          variant="contained"
          color="primary"
          sx={{
            height: "56px",
            fontSize: "1.1rem",
            padding: "16px 24px",
            borderRadius: "4px",
            textTransform: "none",
            minWidth: 140,
          }}
          onClick={handleLoadData}
        >
          Daten laden
        </Button>
      </Box>

      {/* Summary Cards */}
      <Box
        sx={{
          display: "flex",
          gap: 3,
          justifyContent: "center",
          flexWrap: "wrap",
          maxWidth: "1200px",
        }}
      >
        <Card sx={{ minWidth: 300, padding: 2, flex: 1 }}>
          <CardContent>
            <Typography color="text.secondary" variant="h6">
              Umsatz
            </Typography>
            <Typography variant="h2" gutterBottom>
              {totalSold.toFixed(2)} €
            </Typography>
            <LinearProgress
              variant="determinate"
              value={
                timeRange === "custom"
                  ? 0
                  : Math.min(Math.abs(percentTotalSold), 100)
              }
              sx={{
                height: 10,
                borderRadius: 5,
                backgroundColor: "#e0e0e0",
                "& .MuiLinearProgress-bar": {
                  backgroundColor: percentTotalSold < 0 ? "#d32f2f" : "#1976d2",
                },
              }}
            />
            <Typography variant="body2" color="text.secondary" mt={1}>
              {getPercentLabel(percentTotalSold, lastTotalSold)}
            </Typography>
          </CardContent>
        </Card>

        <Card sx={{ minWidth: 300, padding: 2, flex: 1 }}>
          <CardContent>
            <Typography color="text.secondary" variant="h6">
              Transaktionen
            </Typography>
            <Typography variant="h2" gutterBottom>
              {transactionCount}
            </Typography>
            <LinearProgress
              variant="determinate"
              value={
                timeRange === "custom"
                  ? 0
                  : Math.min(Math.abs(percentTransactionCount), 100)
              }
              sx={{
                height: 10,
                borderRadius: 5,
                backgroundColor: "#e0e0e0",
                "& .MuiLinearProgress-bar": {
                  backgroundColor:
                    percentTransactionCount < 0 ? "#d32f2f" : "#1976d2",
                },
              }}
            />
            <Typography variant="body2" color="text.secondary" mt={1}>
              {getPercentLabel(percentTransactionCount, lastTransactionCount)}
            </Typography>
          </CardContent>
        </Card>

        <Card sx={{ minWidth: 300, padding: 2, flex: 1 }}>
          <CardContent>
            <Typography color="text.secondary" variant="h6">
              Ø Transaktionswert
            </Typography>
            <Typography variant="h2" gutterBottom>
              {avgTransaction.toFixed(2)} €
            </Typography>
            <LinearProgress
              variant="determinate"
              value={
                timeRange === "custom"
                  ? 0
                  : Math.min(Math.abs(percentAvgTransaction), 100)
              }
              sx={{
                height: 10,
                borderRadius: 5,
                backgroundColor: "#e0e0e0",
                "& .MuiLinearProgress-bar": {
                  backgroundColor:
                    percentAvgTransaction < 0 ? "#d32f2f" : "#1976d2",
                },
              }}
            />
            <Typography variant="body2" color="text.secondary" mt={1}>
              {getPercentLabel(percentAvgTransaction, lastAvgTransaction)}
            </Typography>
          </CardContent>
        </Card>
      </Box>
    </Box>
  );
};

export default FinanceInsights;
