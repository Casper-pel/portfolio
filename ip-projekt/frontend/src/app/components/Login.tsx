"use client";

import * as React from "react";
import { useTheme } from "@mui/material/styles";
import { useRouter } from "next/navigation";
import { API_BASE_URL } from "./requests/baseUrl";
import { useAuth } from "../context/AuthContext";
import { UserService } from "./services/userService";
import { CookieService } from "./services/cookieService";


import { Box, Button, TextField, Typography } from "@mui/material";
import { useState } from "react";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";

export default function EmployeeSignInPage() {
  const theme = useTheme();
  const router = useRouter();
  const { login } = useAuth();



  const [id, setEmployeeId] = useState("");
  const [password, setPassword] = useState("");
  const [employeeIdError, setEmployeeIdError] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [formError, setFormError] = useState("");

  const anmelden = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setEmployeeIdError("");

    const employeeId = parseInt(id, 10);

    if (password.length === 0) {
      setPasswordError("Passwort darf nicht leer sein!");
      return;
    }

    setPasswordError("");

    try {
      const res = await fetch(`${API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ employeeId, password }),
        mode: "cors",
        credentials: "include",
      });

      if (!res.ok) {
        setFormError("Ein Fehler ist aufgetreten");
        return
      }

      const user = await UserService.getCurrentUser();
      const accessRights = await CookieService.getAccessRights();

      if (user) {
        login(user, accessRights);
        setFormError("");
        router.push("/");
      } else {
        setFormError("Benutzer konnte nicht geladen werden.");
      }
    } catch (_error) {
      setFormError("Es ist ein Fehler aufgetreten. Bitte versuche es erneut.");
    }
    setFormError
  };

  return (
    <Box
      sx={{
        display: "flex",
        bgcolor: "background.paper",
        height: "100vh",
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <Box
        component="form"
        onSubmit={anmelden}
        sx={{
          bgcolor: "background.paper",
          display: "flex",
          flexDirection: "column",
          p: "2.5rem",
          border: "1px solid",
          borderRadius: 1,
          borderColor: theme.palette.grey[400],
          boxShadow: theme.shadows[4],
        }}
      >
        <Typography
          variant="h5"
          color="textPrimary"
          component="h1"
          sx={{
            fontWeight: 600,
            my: theme.spacing(1),
            textAlign: "center",
            width: "100%",
          }}
        >
          Anmelden
        </Typography>
        <Typography
          variant="body2"
          color="textSecondary"
          gutterBottom
          sx={{ textAlign: "center", mb: 0 }}
        >
          Melde dich mit deiner Mitarbeiter-ID und deinem Passwort an.
        </Typography>
        <TextField
          className="employee-id"
          size="small"
          required
          variant="outlined"
          label="Mitarbeiter ID"
          error={!!employeeIdError}
          helperText={employeeIdError}
          value={id}
          onChange={(e) => setEmployeeId(e.target.value)}
          fullWidth
          sx={{ mt: 3 }}
        />
        <TextField
          className="password"
          required
          size="small"
          variant="outlined"
          type="password"
          label="Passwort"
          error={!!passwordError}
          helperText={passwordError}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          fullWidth
          sx={{ mt: 3 }}
        />
        {formError && (
          <Box
            sx={{
              mt: 3,
              display: "flex",
              alignItems: "center",
              height: "2.5rem",
              borderRadius: "4px",
              fontWeight: 400,
              fontSize: "1rem",
              bgcolor: "rgb(253, 237, 237)",
              color: "rgb(95, 33, 32)",
            }}
          >
            <ErrorOutlineIcon
              sx={{
                fontSize: "1.25rem",
                color: "#d32f2f",
                marginLeft: "1rem",
                mr: 1.5,
              }}
            />
            {formError}
          </Box>
        )}
        <Button
          className="submit-button"
          type="submit"
          variant="outlined"
          fullWidth
          sx={{
            mt: 3,
            textTransform: "capitalize",
            bgcolor: "#1976d2",
            color: "#fff",
            "&:hover": {
              bgcolor: "#1565c0",
            },
          }}
        >
          Anmelden
        </Button>
      </Box>
    </Box>
  );
}
