"use client";

import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Stack,
  IconButton,
  Avatar,
  Tooltip,
  Menu,
  MenuItem,
} from "@mui/material";
import { useState } from "react";
import { Keyboard, KeyboardHide } from "@mui/icons-material";
import { useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import { API_BASE_URL } from "../../components/requests/baseUrl";
import { useAuth, User } from "@/app/context/AuthContext";

export default function KassaPage() {
  const router = useRouter();
  const { user, accessRights, logout } = useAuth();

  const [group, setGroup] = useState(true);
  const [showNumpad, setShowNumpad] = useState(false);
  const [eanInput, setEanInput] = useState("");
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const inputRef = useRef<HTMLInputElement>(null);
  const [eanFocused, setEanFocused] = useState(true);
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [editingQuantityIndex, setEditingQuantityIndex] = useState<number | null>(null);
  const [editingDiscountIndex, setEditingDiscountIndex] = useState<number | null>(null);
  const [editingPriceIndex, setEditingPriceIndex] = useState<number | null>(null);
  const [tempQuantity, setTempQuantity] = useState("");
  const [tempDiscount, setTempDiscount] = useState("");
  const [tempPrice, setTempPrice] = useState("");
  const editingQuantityRef = useRef<number | null>(null);
  const editingDiscountRef = useRef<number | null>(null);
  const editingPriceRef = useRef<number | null>(null);
  const containerRefs = useRef<Map<string, HTMLElement>>(new Map());
  const [dateTime, setDateTime] = useState(new Date());
  const [eanError, setEanError] = useState(false);
  const [eanErrorMessage, setEanErrorMessage] = useState("");

  const [anchorElUser, setAnchorElUser] = useState<null | HTMLElement>(null);
  const settings = [
    { label: "Dashboard", action: "dashboard" },
    { label: "Logout", action: "logout" },
  ];

  const setNameRef = (name: string) => (el: HTMLElement | null) => {
    if (el) containerRefs.current.set(name, el);
  };

  interface CartItem {
    ean: string;
    productId: string;
    priceId: string;
    name: string;
    menge: number;
    stattPreis: number;
    istPreis: number;
    rabatt?: number;
  }

  useEffect(() => {
    if (accessRights.length > 0 && !accessRights.includes("kasse") && !accessRights.includes("admin")) {
      router.push("/dashboard");
    }
  }, [accessRights]);

  function calculateTotal(cartItems: CartItem[]): number {
    return cartItems.reduce((sum, item) => {
      const discount = 1 - (item.rabatt ?? 0) / 100;
      const itemTotal = item.istPreis * item.menge * discount;
      return sum + itemTotal;
    }, 0);
  }

  const handleNumpadInput = (
    field: "quantity" | "discount" | "ean" | "istPreis",
    index: number | null,
    num: string
  ) => {
    if (field === "quantity" && index !== null) {
      if (num === "enter") {
        handleItemChange(index, "menge", tempQuantity);
      } else {
        setTempQuantity((prev) => prev + num);
      }
    } else if (field === "discount" && index !== null) {
      if (num === "enter") {
        handleItemChange(index, "rabatt", tempDiscount);
      } else {
        setTempDiscount((prev) => prev + num);
      }
    } else if (field === "istPreis" && index !== null) {
      if (num === "enter") {
        handleItemChange(index, "istPreis", tempPrice);
      } else {
        setTempPrice((prev) => prev + num);
      }
    } else if (field === "ean") {
      if (num === "enter") {
        if (!/^\d+$/.test(eanInput.trim())) {
          setEanError(true);
          return;
        }
        const existingItemIndex = cartItems.findIndex(
          (item) => item.ean === eanInput.trim()
        );
        if (group && existingItemIndex !== -1) {
          const updatedCartItems = [...cartItems];
          updatedCartItems[existingItemIndex].menge += 1;
          setCartItems(updatedCartItems);
        } else {
          fetchProductByEan(eanInput.trim());
        }
        setEanInput("");
        setEanError(false);
        inputRef.current?.focus();
      } else {
        if (/^\d+$/.test(num) || (num === "0" && eanInput.length > 0)) {
          setEanInput((prev) => prev + num);
          setEanError(false);
        } else {
          setEanError(true);
        }
        inputRef.current?.focus();
      }
    }
  };

  useEffect(() => {
    const input = inputRef.current;

    if (!input) return;

    const handleFocus = () => setEanFocused(true);
    const handleBlur = () => setEanFocused(false);

    input.addEventListener("focus", handleFocus);
    input.addEventListener("blur", handleBlur);

    return () => {
      input.removeEventListener("focus", handleFocus);
      input.removeEventListener("blur", handleBlur);
    };
  }, []);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        containerRefs.current &&
        ![...containerRefs.current.values()].some((el) =>
          el.contains(event.target as Node)
        )
      ) {
        setTimeout(() => {
          inputRef.current?.focus();
        }, 0);
        setSelectedIndex(null);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const toggleNumpad = () => {
    setShowNumpad((prev) => {
      const next = !prev;
      if (next && !eanFocused) {
        setTimeout(() => {
          inputRef.current?.focus();
        }, 0);
      }
      return next;
    });
  };

  const handleItemChange = (
    index: number,
    field: "menge" | "rabatt" | "istPreis",
    tempValue: string
  ) => {
    const newValue = parseFloat(tempValue);
    if (
      !isNaN(newValue) &&
      newValue >= 0 &&
      (field !== "rabatt" || newValue <= 100)
    ) {
      const updatedItems = [...cartItems];
      const item = { ...updatedItems[index] };
      item[field] = newValue;
      updatedItems[index] = item;
      setCartItems(updatedItems);
    }
    setEditingQuantityIndex(null);
    setEditingDiscountIndex(null);
    setEditingPriceIndex(null);

    editingDiscountRef.current = null;
    editingQuantityRef.current = null;
    editingPriceRef.current = null;

    setTempQuantity("");
    setTempDiscount("");
    setTempPrice("");

    setTimeout(() => {
      inputRef.current?.focus();
    }, 0);
  };

  const cancelEditByField = (field: "menge" | "rabatt" | "istPreis") => {
    const mapping = {
      menge: [setEditingQuantityIndex, editingQuantityRef],
      rabatt: [setEditingDiscountIndex, editingDiscountRef],
      istPreis: [setEditingPriceIndex, editingPriceRef],
    } as const;

    const [setter, ref] = mapping[field];
    setter(null);
    ref.current = null;
    setTempQuantity("");
    setTempDiscount("");
    setTempPrice("");

    setTimeout(() => {
      inputRef.current?.focus();
    }, 0);
  };

  useEffect(() => {
    const interval = setInterval(() => {
      setDateTime(new Date());
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const products = cartItems.map((item) => ({
      productId: item.productId,
      quantity: item.menge,
      price: item.priceId,
    }));
    console.log("Produkte zum Checkout:", products);

    const totalPrice = calculateTotal(cartItems);
    const now = new Date();
    const formattedDateTime = now.toISOString(); // z.B. "2024-07-01T12:34:56.789Z"
    const employeeId = user?.employeeId ?? null;
    const productNames = cartItems.map((item) => item.name);

    const kassaCheckoutPayload = {
      totalPrice,
      date: formattedDateTime,
      employeeId,
      productNames,
    };

    try {
      const response = await fetch(`${API_BASE_URL}/kassa/checkout`, {
        method: "POST",
        credentials: "include", // Include cookies for authentication
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(kassaCheckoutPayload),
      });
      if (!response.ok) {
        throw new Error("Fehler beim Kassa-Checkout");
      }
      const data = await response.json();
      console.log("Kassa-Checkout erfolgreich:", data);
    } catch (error) {
      console.error(error);
    }

    sessionStorage.setItem("checkoutProducts", JSON.stringify(products));
    router.push("/checkout"); 
  };

  const fetchProductByEan = async (ean: string) => {
    try {
      const response = await fetch(`${API_BASE_URL}/products/cache/product/ean/${ean}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include", // Include cookies for authentication
      });
      if (response.status === 204) {
        setEanError(true);
        setEanErrorMessage("Kein Produkt mit diesem EAN gefunden!");
        return;
      }
      if (!response.ok) {
        setEanError(true);
        setEanErrorMessage("Fehler beim Laden des Produkts!");
        return;
      }
      const product = await response.json();
      console.log("Produkt geladen:", product);
      setCartItems((prev) => [
        ...prev,
        {
          ean: product.upcCode,
          productId: product.productId,
          priceId: product.priceId,
          name: product.productName,
          menge: 1,
          stattPreis: product.listPrice,
          istPreis: product.listPrice,
          rabatt: 0,
        },
      ]);
      setEanError(false);
      setEanErrorMessage("");
    } catch (error) {
      setEanError(true);
      setEanErrorMessage("Netzwerkfehler oder Server nicht erreichbar!");
    }
  };

  const handleLogout = async () => {
    logout()
  };

  const handleOpenUserMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElUser(event.currentTarget);
  };

  const handleCloseUserMenu = () => {
    setAnchorElUser(null);
  };

  const handleUserMenuClick = async (setting: string) => {
    handleCloseUserMenu();
    if (setting === "logout") {
      await handleLogout();
    } else if (setting === "dashboard") {
      router.push("/dashboard");
    }
  };

  return (
    <Box sx={{ width: "100%", height: "100vh" }}>
      <Box
        sx={{
          height: "10vh",
          bgcolor: "#1976d2",
          color: "white",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          px: 3,
        }}
      >
        <Typography variant="h2" sx={{ fontWeight: "bold", ml: "5%" }}>
          BINGO
        </Typography>
        <Box sx={{ display: "flex", alignItems: "center", gap: 5, mr: "5%" }}>
          <Typography variant="body1">
            {dateTime.toLocaleDateString()} {dateTime.toLocaleTimeString()}
          </Typography>
          <Typography variant="body1">
            {user
              ? `${user.firstName} ${user.lastName}`
              : "Loading..."}
          </Typography>
          <Box
            sx={{
              flexGrow: 0,
              display: "flex",
              alignItems: "center",
              gap: 1,
            }}
          >
            <Tooltip
              title={
                user
                  ? `${user.firstName} ${user.lastName}`
                  : "User settings"
              }
            >
              <IconButton
                className="icon-button"
                onClick={handleOpenUserMenu}
                sx={{ p: 0 }}
              >
                <Avatar
                  alt={
                    user
                      ? `${user.firstName} ${user.lastName}`
                      : "User"
                  }
                >
                  {user
                    ? `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`
                    : "U"}
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
              {settings.map((setting) => (
                <MenuItem
                  key={setting.action}
                  onClick={() => handleUserMenuClick(setting.action)}
                >
                  <Typography sx={{ textAlign: "center" }}>
                    {setting.label}
                  </Typography>
                </MenuItem>
              ))}
            </Menu>
          </Box>
        </Box>
      </Box>

      <Box
        sx={{
          width: "100%",
          height: "90vh",
          display: "flex",
          alignItems: "center",
          gap: 1,
        }}
      >
        <Box sx={{ width: "57%", ml: "1%" }}>
          <Paper
            elevation={3}
            style={{
              height: "80vh",
              padding: 16,
              display: "flex",
              flexDirection: "column",
            }}
          >
            <Box flexGrow={1} overflow="auto">
              <Box
                sx={{
                  display: "grid",
                  gridTemplateColumns: "2fr 2fr 1fr 1fr 1fr 1fr",
                  fontWeight: "bold",
                  mb: 1,
                  px: 1,
                  position: "sticky",
                  top: 0,
                  zIndex: 1,
                  borderBottom: "2px solid #1976d2",
                  bgcolor: "white",
                }}
              >
                <Typography>EAN</Typography>
                <Typography align="left">Name</Typography>
                <Typography align="center">Menge</Typography>
                <Typography align="center">Rabatt</Typography>
                <Typography align="center">Statt-Preis</Typography>
                <Typography align="center">Ist-Preis</Typography>
              </Box>

              <Stack spacing={1} ref={setNameRef("listRef")}>
                {cartItems.map((item, index) => {
                  const isSelected = selectedIndex === index;

                  return (
                    <Box
                      className="cart-item"
                      key={index}
                      onClick={() => setSelectedIndex(index)}
                      sx={{
                        display: "grid",
                        gridTemplateColumns: "2fr 2fr 1fr 1fr 1fr 1fr",
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        cursor: "pointer",
                        bgcolor: isSelected ? "#1976d2" : "transparent",
                        "&:hover": {
                          bgcolor: isSelected ? "#1976d2" : "grey.100",
                        },
                      }}
                    >
                      <Typography>{item.ean}</Typography>
                      <Typography align="left" className="cart-item-name">
                        {item.name}
                      </Typography>
                      <Box
                        className="cart-quantity-box"
                        sx={{
                          display: "flex",
                          justifyContent: "center",
                          alignItems: "center",
                        }}
                        onClick={(e) => {
                          e.stopPropagation();
                          setTempQuantity(""); 
                          setSelectedIndex(index);
                          setEditingQuantityIndex(index);
                          editingQuantityRef.current = index;
                        }}
                      >
                        {editingQuantityIndex === index ? (
                          <input
                            className="cart-quantity-input"
                            type="text"
                            inputMode="numeric"
                            pattern="[0-9]*"
                            value={tempQuantity}
                            onChange={(e) => setTempQuantity(e.target.value)}
                            onBlur={() =>
                              handleItemChange(index, "menge", tempQuantity)
                            }
                            onKeyDown={(e) => {
                              if (e.key === "Enter")
                                handleItemChange(index, "menge", tempQuantity);
                              if (e.key === "Escape")
                                cancelEditByField("menge");
                            }}
                            autoFocus
                            style={{
                              width: "60px",
                              textAlign: "center",
                              fontSize: "1rem",
                              padding: "4px",
                              border: "1px solid #ccc",
                              borderRadius: "4px",
                            }}
                            onClick={(e) => e.stopPropagation()}
                          />
                        ) : (
                          <Typography align="center">{item.menge}</Typography>
                        )}
                      </Box>
                      <Box
                        className="cart-discount-box"
                        sx={{
                          display: "flex",
                          justifyContent: "center",
                          alignItems: "center",
                        }}
                        onClick={(e) => {
                          e.stopPropagation();
                          setTempDiscount(""); 
                          setSelectedIndex(index);
                          setEditingDiscountIndex(index);
                          editingDiscountRef.current = index;
                        }}
                      >
                        {editingDiscountIndex === index ? (
                          <input
                            type="text"
                            inputMode="decimal"
                            pattern="[0-9]*"
                            value={tempDiscount}
                            onChange={(e) => setTempDiscount(e.target.value)}
                            onBlur={() =>
                              handleItemChange(index, "rabatt", tempDiscount)
                            }
                            onKeyDown={(e) => {
                              if (e.key === "Enter")
                                handleItemChange(index, "rabatt", tempDiscount);
                              if (e.key === "Escape")
                                cancelEditByField("rabatt");
                            }}
                            autoFocus
                            style={{
                              width: "60px",
                              textAlign: "center",
                              fontSize: "1rem",
                              padding: "4px",
                              border: "1px solid #ccc",
                              borderRadius: "4px",
                            }}
                            onClick={(e) => e.stopPropagation()}
                          />
                        ) : (
                          <Typography align="center">
                            {item.rabatt ?? 0}%
                          </Typography>
                        )}
                      </Box>
                      <Typography align="center">
                        {item.stattPreis.toFixed(2) ?? 0.0} €
                      </Typography>{" "}
                      <Box
                        sx={{
                          display: "flex",
                          justifyContent: "center",
                          alignItems: "center",
                        }}
                        onClick={(e) => {
                          e.stopPropagation();
                          setTempPrice(""); 
                          setSelectedIndex(index);
                          setEditingPriceIndex(index);
                          editingPriceRef.current = index;
                        }}
                      >
                        {editingPriceIndex === index ? (
                          <input
                            type="text"
                            inputMode="decimal"
                            pattern="[0-9]*"
                            value={tempPrice}
                            onChange={(e) => setTempPrice(e.target.value)}
                            onBlur={() =>
                              handleItemChange(index, "istPreis", tempPrice)
                            }
                            onKeyDown={(e) => {
                              if (e.key === "Enter")
                                handleItemChange(index, "istPreis", tempPrice);
                              if (e.key === "Escape")
                                cancelEditByField("istPreis");
                            }}
                            autoFocus
                            style={{
                              width: "60px",
                              textAlign: "center",
                              fontSize: "1rem",
                              padding: "4px",
                              border: "1px solid #ccc",
                              borderRadius: "4px",
                            }}
                            onClick={(e) => e.stopPropagation()}
                          />
                        ) : (
                          <Typography align="center">
                            {(
                              item.istPreis *
                              (1 - (item.rabatt ?? 0) / 100)
                            ).toFixed(2)}{" "}
                            €
                          </Typography>
                        )}
                      </Box>
                    </Box>
                  );
                })}
              </Stack>
            </Box>
            <Box mt={2}>
              <TextField
                className="ean-input"
                inputRef={inputRef}
                label="EAN-Code"
                fullWidth
                variant="outlined"
                value={eanInput}
                error={eanError}
                helperText={eanError ? eanErrorMessage : ""}
                onFocus={() => setEanFocused(true)}
                onBlur={() => setEanFocused(false)}
                onChange={(e) => {
                  const value = e.target.value;
                  setEanInput(value);
                  if (value === "") {
                    setEanError(false);
                    setEanErrorMessage("");
                  } else if (/^\d+$/.test(value.trim())) {
                    setEanError(false);
                    setEanErrorMessage("");
                  } else {
                    setEanError(true);
                    setEanErrorMessage("Nur Zahlen erlaubt!");
                  }
                }}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && eanInput.trim()) {
                    if (!/^\d+$/.test(eanInput.trim())) {
                      setEanError(true);
                      setEanErrorMessage("Nur Zahlen erlaubt!");
                      return;
                    }
                    const existingItemIndex = cartItems.findIndex(
                      (item) => item.ean === eanInput.trim()
                    );
                    if (group && existingItemIndex !== -1) {
                      const updatedCartItems = [...cartItems];
                      updatedCartItems[existingItemIndex].menge += 1; 
                      setCartItems(updatedCartItems);
                      setEanError(false);
                      setEanErrorMessage("");
                    } else {
                      fetchProductByEan(eanInput.trim());
                    }
                    setEanInput(""); 
                  }
                }}
              />
            </Box>
          </Paper>
        </Box>

        <Box sx={{ width: "40%", mr: "1%" }}>
          <Paper
            elevation={3}
            style={{
              height: "80vh",
              padding: 16,
              display: "flex",
              flexDirection: "column",
            }}
          >
            <Box sx={{ mb: 2 }}>
              <Typography
                className="total-price"
                variant="h6"
                align="center"
                sx={{ fontSize: "2rem", mt: "5%" }}
              >
                Gesamt: {calculateTotal(cartItems).toFixed(2) ?? 0.0} €
              </Typography>
            </Box>
            <Stack
              spacing={2}
              sx={{ flexGrow: 1, justifyContent: "end", mb: "3%" }}
            >
              <Button
                className="group-button"
                variant="outlined"
                fullWidth
                onMouseDown={(e) => {
                  e.preventDefault();
                  setGroup(!group);
                }}
                sx={{
                  color: group ? "green" : "error.main",
                  borderColor: group ? "green" : "error.main",
                  "&:hover": {
                    backgroundColor: group
                      ? "rgba(0, 128, 0, 0.04)"
                      : "rgba(211, 47, 47, 0.04)",
                    borderColor: group ? "darkgreen" : "error.dark",
                  },
                }}
              >
                Artikel Gruppieren
              </Button>
              <Button
                className="delete-cart-button"
                variant="outlined"
                fullWidth
                color="error"
                onMouseDown={(e) => {
                  setCartItems([]);
                }}
              >
                Korb Löschen
              </Button>

              <Button
                className="delete-button"
                ref={setNameRef("delete")}
                variant="outlined"
                color="error"
                fullWidth
                onMouseDown={(e) => {
                  e.preventDefault();
                  if (editingQuantityRef.current !== null) {
                    setTempQuantity((prev) => prev.slice(0, -1));
                  } else if (editingDiscountRef.current !== null) {
                    setTempDiscount((prev) => prev.slice(0, -1));
                  } else if (editingPriceRef.current !== null) {
                    setTempPrice((prev) => prev.slice(0, -1));
                  } else if (selectedIndex !== null) {
                    setCartItems((prev) =>
                      prev.filter((_, i) => i !== selectedIndex)
                    );
                    setSelectedIndex(null); 
                    setEditingDiscountIndex(null);
                    setEditingQuantityIndex(null);
                    setEditingPriceIndex(null);
                    editingDiscountRef.current = null;
                    editingQuantityRef.current = null;
                    editingPriceRef.current = null;
                  } else if (eanFocused) {
                    setEanInput((prev) => prev.slice(0, -1));
                  }
                }}
              >
                Löschen
              </Button>
              <Button variant="contained" fullWidth onClick={onSubmit}>
                Abschließen
              </Button>
            </Stack>
            <IconButton
              className="numpad-toggle-button"
              onClick={toggleNumpad}
              onMouseDown={(e) => e.preventDefault()} 
              sx={{ alignSelf: "end", marginTop: "auto", m: 0 }}
            >
              {showNumpad ? <KeyboardHide /> : <Keyboard />}
            </IconButton>
            {showNumpad && (
              <Box
                ref={setNameRef("numpad")}
                sx={{
                  marginTop: "auto",
                  display: "grid",
                  gridTemplateColumns: "repeat(3, 1fr)",
                  gap: 1,
                  mt: "3%",
                }}
              >
                {[
                  "1",
                  "2",
                  "3",
                  "4",
                  "5",
                  "6",
                  "7",
                  "8",
                  "9",
                  ".",
                  "0",
                  "enter",
                ].map((num) => (
                  <Button
                    className={`numpad-button-${num}`}
                    variant="outlined"
                    fullWidth
                    key={num}
                    onMouseDown={(e) => e.preventDefault()}
                    onClick={() =>
                      handleNumpadInput(
                        editingQuantityRef.current !== null
                          ? "quantity"
                          : editingDiscountRef.current !== null
                            ? "discount"
                            : editingPriceRef.current !== null
                              ? "istPreis"
                              : "ean",
                        editingQuantityRef.current ??
                        editingDiscountRef.current ??
                        editingPriceRef.current ??
                        null,
                        num
                      )
                    }
                  >
                    {num}
                  </Button>
                ))}
              </Box>
            )}
          </Paper>
        </Box>
      </Box>
    </Box>
  );
}
