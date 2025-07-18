"use client";

import "../globals.css";
import * as React from "react";
import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Toolbar from "@mui/material/Toolbar";
import IconButton from "@mui/material/IconButton";
import Typography from "@mui/material/Typography";
import Menu from "@mui/material/Menu";
import MenuIcon from "@mui/icons-material/Menu";
import Container from "@mui/material/Container";
import Avatar from "@mui/material/Avatar";
import Button from "@mui/material/Button";
import Tooltip from "@mui/material/Tooltip";
import MenuItem from "@mui/material/MenuItem";
import AdbIcon from "@mui/icons-material/Adb";
import { useRouter } from "next/navigation";
import { useAuth } from "../context/AuthContext";
import Link from "next/link";

const settings = [
  { name: "Menü" },
  { name: "Kasse", requiredRight: "kasse" },
  { name: "Logout" }
];

const pages = [
  { name: "Produkte", path: "/dashboard/products", requiredRight: "product.read" },
  { name: "Mitarbeiter", path: "/dashboard/employees", requiredRight: "user.read" },
  { name: "Finanzen", path: "/dashboard/finance", requiredRight: "finances" },
  { name: "Rollen", path: "/dashboard/roles", requiredRight: "role.read" },
  { name: "Urlaub", path: "/dashboard/urlaub" },
  { name: "Coupons", path: "/dashboard/coupons", requiredRight: "coupons.read" },
];

function ResponsiveAppBar() {
  const router = useRouter();

  const { accessRights, user, isLoading, isAuthenticated, logout } = useAuth();

  const [anchorElNav, setAnchorElNav] = React.useState<null | HTMLElement>(
    null
  );
  const [anchorElUser, setAnchorElUser] = React.useState<null | HTMLElement>(
    null
  );

  const handleOpenNavMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElNav(event.currentTarget);
  };
  const handleOpenUserMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElUser(event.currentTarget);
  };

  const handleCloseNavMenu = () => {
    setAnchorElNav(null);
  };

  const handleCloseUserMenu = () => {
    setAnchorElUser(null);
  };

  const handleLogin = async (setting: string) => {
    if (setting === "Log In") {
      router.push("/login");
    } else if (setting === "Kasse") {
      router.push("/kassa");
    } else if (setting === "Menü") {
      router.push("/");
    } else if (setting === "Logout") {
      try {
        // Verwende den AuthContext logout
        await logout();
      } catch (error) {
        console.error("Logout error:", error);
        // Fallback: Direkte Umleitung zur Login-Seite
        router.push("/login");
      }
    }
  };

  const handleNavigation = (path: string) => {
    router.push(path);
    handleCloseNavMenu();
  };

  return (
    <AppBar position="static">
      <Container maxWidth="xl">
        <Toolbar disableGutters>
          <AdbIcon sx={{ display: { xs: "none", md: "flex" }, mr: 1 }} />
          <Link href="/" passHref legacyBehavior>
            <Typography
              variant="h6"
              noWrap
              component="a"
              sx={{
                mr: 2,
                display: { xs: "none", md: "flex" },
                fontFamily: "monospace",
                fontWeight: 700,
                letterSpacing: ".3rem",
                color: "inherit",
                textDecoration: "none",
              }}
            >
              BINGO
            </Typography>
          </Link>



          <Box sx={{ flexGrow: 1, display: { xs: "flex", md: "none" } }}>
            <IconButton
              size="large"
              aria-label="account of current user"
              aria-controls="menu-appbar"
              aria-haspopup="true"
              onClick={handleOpenNavMenu}
              color="inherit"
            >
              <MenuIcon />
            </IconButton>
            <Menu
              id="menu-appbar"
              anchorEl={anchorElNav}
              anchorOrigin={{
                vertical: "bottom",
                horizontal: "left",
              }}
              keepMounted
              transformOrigin={{
                vertical: "top",
                horizontal: "left",
              }}
              open={Boolean(anchorElNav)}
              onClose={handleCloseNavMenu}
              sx={{ display: { xs: "block", md: "none" } }}
            >
              {pages.map((page) => (
                <MenuItem
                  key={page.name}
                  onClick={() => handleNavigation(page.path)}
                >
                  <Typography sx={{ textAlign: "center" }}>
                    {page.name}
                  </Typography>
                </MenuItem>
              ))}
            </Menu>
          </Box>
          <AdbIcon sx={{ display: { xs: "flex", md: "none" }, mr: 1 }} />
          <Link href="/" passHref legacyBehavior>
            <Typography
              variant="h5"
              noWrap
              component="a"
              sx={{
                mr: 2,
                display: { xs: "flex", md: "none" },
                flexGrow: 1,
                fontFamily: "monospace",
                fontWeight: 700,
                letterSpacing: ".3rem",
                color: "inherit",
                textDecoration: "none",
              }}
            >
              BINGO
            </Typography>
          </Link>


          <Box sx={{ flexGrow: 1, display: { xs: "none", md: "flex" } }}>
            {pages
              .filter(page =>
                !page.requiredRight ||
                (accessRights && (accessRights.includes(page.requiredRight) || accessRights.includes("admin")))
              )
              .map(page => (
                <Button
                  key={page.name}
                  onClick={() => handleNavigation(page.path)}
                  sx={{ my: 2, color: "white", display: "block" }}
                >
                  {page.name}
                </Button>
              ))
            }
          </Box>
          <Box sx={{ flexGrow: 0, display: 'flex', alignItems: 'center', gap: 1 }}>
            {user && (
              <Typography variant="body2" sx={{ color: 'white', display: { xs: 'none', sm: 'block' } }}>
                {user.firstName} {user.lastName}
              </Typography>
            )}
            <Tooltip title={user ? `${user.firstName} ${user.lastName}` : "User settings"}>
              <IconButton onClick={handleOpenUserMenu} sx={{ p: 0 }}>
                <Avatar alt={user ? `${user.firstName} ${user.lastName}` : "User"}>
                  {user ? `${user.firstName.charAt(0)}${user.lastName.charAt(0)}` : "U"}
                </Avatar>
              </IconButton>
            </Tooltip>
            <Menu
              sx={{ mt: "45px" }}
              id="menu-appbar"
              anchorEl={anchorElUser}
              anchorOrigin={{
                vertical: "top",
                horizontal: "right",
              }}
              keepMounted
              transformOrigin={{
                vertical: "top",
                horizontal: "right",
              }}
              open={Boolean(anchorElUser)}
              onClose={handleCloseUserMenu}
            >
              {settings
                .filter(s =>
                  // kein Recht nötig ODER Recht vorhanden ODER Admin
                  !s.requiredRight ||
                  accessRights?.includes(s.requiredRight) ||
                  accessRights?.includes("admin")
                )
                .map(s => (
                  <MenuItem
                    key={s.name}
                    onClick={() => {
                      handleCloseUserMenu();
                      handleLogin(s.name);
                    }}
                  >
                    <Typography textAlign="center">
                      {s.name}
                    </Typography>
                  </MenuItem>
                ))
              }
            </Menu>

          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
}
export default ResponsiveAppBar;
