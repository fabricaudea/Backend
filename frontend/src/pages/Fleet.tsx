import React from 'react';
import { Navigation } from '@/components/Navigation';
import { VehicleTable } from '@/components/VehicleTable';

export const Fleet: React.FC = () => {
  return (
    <Navigation>
      <VehicleTable />
    </Navigation>
  );
};