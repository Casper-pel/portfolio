import React, { useState, ChangeEvent } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Button,
} from "@mui/material";
import { createCoupon } from "../requests/CouponRequests";

type Props = {
  open: boolean;
  onClose: () => void;
};

export default function NewCouponDialog({ open, onClose}: Props) {
  const [form, setForm] = useState({
    name: "",
    amountOff: 0,
    percentOff: 0,
    currency: "EUR",
    duration: "once",
  });         

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: name === "amountOff" || name === "percentOff" ? parseInt(value) || 0 : value,
    }));
  };

  const handleSubmit = async () => {
    if (
      (form.amountOff > 0 && form.percentOff > 0) ||
      (form.amountOff === 0 && form.percentOff === 0)
    ) {
      alert("Bitte entweder 'amountOff' ODER 'percentOff' setzen – aber nicht beides.");
      return;
    }

    try {
      const response = await createCoupon(form);
      if(!response) {
        return;
      }
      if (response.status === 200) {

        onClose(); // Dialog schließen
        setForm({
          name: "",
          amountOff: 0,
          percentOff: 0,
          currency: "EUR",
          duration: "once",
        });
      } else {
        alert("Fehler beim Erstellen des Coupons");
      }
    } catch (err) {
      console.error("Fehler beim Erstellen:", err);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Neuen Coupon erstellen</DialogTitle>
      <DialogContent>
        <TextField
          margin="dense"
          label="Name"
          name="name"
          value={form.name}
          onChange={handleChange}
          fullWidth
          required
        />
        <TextField
          margin="dense"
          label="Amount Off (in Cent)"
          name="amountOff"
          type="number"
          value={form.amountOff}
          onChange={handleChange}
          fullWidth
        />
        <TextField
          margin="dense"
          label="Percent Off"
          name="percentOff"
          type="number"
          value={form.percentOff}
          onChange={handleChange}
          fullWidth
        />
        <TextField
          margin="dense"
          label="Währung"
          name="currency"
          value={form.currency}
          onChange={handleChange}
          select
          fullWidth
        >
          <MenuItem value="EUR">EUR (€)</MenuItem>
          <MenuItem value="USD">USD ($)</MenuItem>
        </TextField>
        <TextField
          margin="dense"
          label="Dauer"
          name="duration"
          value={form.duration}
          onChange={handleChange}
          select
          fullWidth
        >
          <MenuItem value="once">Once</MenuItem>
          <MenuItem value="forever">Forever</MenuItem>
        </TextField>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button onClick={handleSubmit} variant="contained">Erstellen</Button>
      </DialogActions>
    </Dialog>
  );
}
