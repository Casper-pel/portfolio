"use client";

import React, { useState } from 'react';
import { Box, Tabs, Tab, Typography } from '@mui/material';
import UrlaubsantragModal from './UrlaubsantragModal';
import AntragTabelle from './AntragTabelle';
import MeineAntraege from './MeineAntraege';
import { useAuth } from '@/app/context/AuthContext';

interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

function TabPanel(props: TabPanelProps) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`urlaubsverwaltung-tabpanel-${index}`}
            aria-labelledby={`urlaubsverwaltung-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box sx={{ p: 3 }}>
                    {children}
                </Box>
            )}
        </div>
    );
}

function a11yProps(index: number) {
    return {
        id: `urlaubsverwaltung-tab-${index}`,
        'aria-controls': `urlaubsverwaltung-tabpanel-${index}`,
    };
}

const UrlaubsVerwaltung: React.FC = () => {
    const [value, setValue] = useState(0);

    const { accessRights } = useAuth();

    const handleChange = (event: React.SyntheticEvent, newValue: number) => {
        setValue(newValue);
    };

    return (
        <Box sx={{
            p: 3,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            width: '100%',
            maxWidth: 1200,
            mx: 'auto'
        }}>
            <Typography variant="h4" gutterBottom align="center" sx={{ width: '100%' }}>
                Urlaubsverwaltung
            </Typography>

            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                <Tabs value={value} onChange={handleChange} aria-label="urlaubsverwaltung tabs">
                    <Tab label="Neuen Antrag stellen" {...a11yProps(0)} />
                    <Tab label="Meine Anträge" {...a11yProps(1)} />
                    {(accessRights.includes("urlaub.read") || accessRights.includes("admin")) && <Tab label="Anträge verwalten" {...a11yProps(2)} />}
                </Tabs>
            </Box>

            <TabPanel value={value} index={0}>
                <Box sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    minHeight: '400px',
                    justifyContent: 'center'
                }}>
                    <Typography variant="h6" gutterBottom>
                        Urlaubsantrag einreichen
                    </Typography>
                    <Typography id="info-text" variant="body1" color="text.secondary" sx={{ mb: 3, textAlign: 'center' }}>
                        Stellen Sie hier Ihren Urlaubsantrag. Alle Anträge werden von Ihrem Vorgesetzten geprüft.
                    </Typography>
                    <UrlaubsantragModal />
                </Box>
            </TabPanel>

            <TabPanel value={value} index={1}>
                <MeineAntraege />
            </TabPanel>

            <TabPanel value={value} index={2}>
                <AntragTabelle />
            </TabPanel>
        </Box>
    );
};

export default UrlaubsVerwaltung;
