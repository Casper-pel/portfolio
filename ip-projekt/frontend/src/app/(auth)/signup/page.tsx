"use client";

import {
  alpha,
  Box,
  Typography,
  useTheme,
  TextField,
  Stack,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  IconButton,
} from "@mui/material";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { CloseSharp } from "@mui/icons-material";
import { API_BASE_URL } from "../../components/requests/baseUrl";
import { Role } from "../../components/model/role";
import { useAuth } from "@/app/context/AuthContext";
import { RoleService } from "@/app/components/services/roleService";

export default function SignupPage() {
  const router = useRouter();
  const { accessRights, isLoading } = useAuth();
  const theme = useTheme();

  const [role, setRole] = useState("");
  const [roles, setRoles] = useState<Role[]>([]);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [id, setEmployeeId] = useState("");
  const [password, setPassword] = useState("");
  const [repeatPassword, setRepeatPassword] = useState("");
  const [employeeIdError, setEmployeeIdError] = useState("");
  const [repeatPasswordError, setRepeatPasswordError] = useState("");
  const [formError, setFormError] = useState("");

  useEffect(() => {
    if (accessRights.length > 0 && !accessRights.includes("user.create") && !accessRights.includes("admin")) {
      window.location.href = "/dashboard";
    }
  }, [accessRights]);

  useEffect(() => {
    fetchRoles();
  }, []);

  const fetchRoles = async () => {
    try {
      const tRoles = await RoleService.getAllRoles();
      setRoles(
        tRoles.map((role) => ({
          roleId: role.roleId,
          roleName: role.roleName,
          description: role.description,
          rolePermissions: role.rolePermissions,
          employees: [], // Default empty array for missing 'employees' property
        }))
      );
    } catch (err) {
      console.error('Error fetching roles:', err);
      setFormError('Fehler beim Laden der Rollen');
    }
  };

  const handleChange = (event: SelectChangeEvent) => {
    setRole(event.target.value);
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!/^\d+$/.test(id)) {
      setEmployeeIdError("ID muss eine Zahl sein!");
      return;
    }

    setEmployeeIdError("");

    const employeeId = parseInt(id, 10);

    if (password != repeatPassword) {
      setRepeatPasswordError("Passwörter sind nicht identisch!");
      return;
    }

    setRepeatPasswordError("");

    try {
      const selectedRoleObj = role ? roles.find(r => 
        r.roleId && r.roleId.toString() === role
      ) || null : null;

      const requestBody = {
        employeeId,
        password,
        firstName,
        lastName,
        role: selectedRoleObj
      };

      console.log("Request body:", JSON.stringify(requestBody, null, 2));

      const res = await fetch(`${API_BASE_URL}/auth/signup`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(requestBody),
        mode: "cors",
        credentials: "include",
      });

      const data = await res.json();

      if (!res.ok || !data.success) {
        setFormError(data.error || "Ein fehler ist aufgetreten");
        return;
      }
      setFormError("");
      router.push("/dashboard/employees");
    } catch (_error) {
      setFormError("Etwas hat nicht geklappt");
      return;
    }
    setFormError("");
  };

  if(isLoading) {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "100vh",
        }}
      >
        <Typography variant="h6">Lade...</Typography>
      </Box>
    );
  }

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
      <IconButton className="close" sx={{ position: "absolute", top: 16, right: 16 }} onClick={() => router.push("/dashboard/employees")}>
        <CloseSharp />
      </IconButton>
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{
          bgcolor: "background.paper",
          display: "flex",
          flexDirection: "column",
          p: "2.5rem",
          border: "1px solid",
          borderRadius: 1,
          borderColor: alpha(theme.palette.grey[400], 0.4),
          boxShadow: theme.shadows[4],
        }}
      >
        <Box>
          <Typography
            variant="h5"
            color="textPrimary"
            component="h1"
            sx={{
              fontWeight: 600,
              my: theme.spacing(1),
              textAlign: "center",
              //   bgcolor: "blue",
              width: "100%",
            }}
          >
            Neuen Nutzer anlegen
          </Typography>
          <Typography
            variant="body2"
            color="textSecondary"
            gutterBottom
            sx={{ textAlign: "center" }}
          >
            Erstelle ein neues Nutzerprofil für einen neuen Mitarbeiter
          </Typography>
        </Box>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          sx={{
            mt: 3,
          }}
        >
          <TextField
            className="first-name"
            required
            variant="outlined"
            label="Vorname"
            size="small"
            onChange={(e) => setFirstName(e.target.value)}
            fullWidth
            sx={{
              mt: 2,
            }}
          />
          <TextField
            className="last-name"
            size="small"
            required
            variant="outlined"
            label="Nachname"
            onChange={(e) => setLastName(e.target.value)}
            fullWidth
            sx={{ mt: 2 }}
          />
        </Stack>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          sx={{
            mt: 3,
          }}
        >
          <TextField
            className="employee-id"
            size="small"
            required
            variant="outlined"
            label="Mitarbeiter ID"
            error={!!employeeIdError}
            helperText={employeeIdError}
            onChange={(e) => setEmployeeId(e.target.value)}
            fullWidth
            sx={{ mt: 2 }}
          />
          <FormControl fullWidth>
            <InputLabel size="small">Rolle</InputLabel>
            <Select
              className="role"
              size="small"
              value={role}
              label="Rolle"
              onChange={handleChange}
            >
              <MenuItem value="">
                <em>Keine Rolle</em>
              </MenuItem>
              {roles.map((roleItem) => (
                <MenuItem key={roleItem.roleId} value={(roleItem !== null && roleItem.roleId !== null) ? roleItem.roleId.toString() : ""}>
                  {roleItem.roleName}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Stack>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          sx={{
            mt: 3,
          }}
        >
          <TextField
            className="password"
            required
            size="small"
            variant="outlined"
            type="password"
            label="Passwort"
            onChange={(e) => setPassword(e.target.value)}
            fullWidth
            sx={{ mt: 2 }}
          />
          <TextField
            className="repeat-password"
            size="small"
            required
            variant="outlined"
            type="password"
            label="Passwort wiederholen"
            error={!!repeatPasswordError}
            helperText={repeatPasswordError}
            onChange={(e) => setRepeatPassword(e.target.value)}
            fullWidth
            sx={{ mt: 2 }}
          />
        </Stack>
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
        <Box>
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
            Nutzer anlegen
          </Button>
        </Box>
      </Box>
    </Box>
  );
}
