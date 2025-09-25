import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import { VehicleProvider } from "@/contexts/VehicleContext";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { Login } from "@/pages/Login";
import { Fleet } from "@/pages/Fleet";
import { Alerts } from "@/pages/Alerts";
import { Reports } from "@/pages/Reports";
import { Settings } from "@/pages/Settings";
import { Forbidden } from "@/pages/Forbidden";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <AuthProvider>
      <VehicleProvider>
        <TooltipProvider>
          <Toaster />
          <Sonner />
          <BrowserRouter>
            <Routes>
              <Route path="/" element={<Login />} />
              <Route path="/login" element={<Login />} />
              
              {/* Protected Routes */}
              <Route path="/fleet" element={
                <ProtectedRoute>
                  <Fleet />
                </ProtectedRoute>
              } />
              
              <Route path="/alerts" element={
                <ProtectedRoute>
                  <Alerts />
                </ProtectedRoute>
              } />
              
              <Route path="/reports" element={
                <ProtectedRoute allowedRoles={['administrador']}>
                  <Reports />
                </ProtectedRoute>
              } />
              
              <Route path="/settings" element={
                <ProtectedRoute allowedRoles={['administrador']}>
                  <Settings />
                </ProtectedRoute>
              } />
              
              {/* Error Pages */}
              <Route path="/403" element={<Forbidden />} />
              
              {/* Legacy Dashboard Route - Redirect based on role */}
              <Route path="/dashboard" element={
                <ProtectedRoute>
                  <Fleet />
                </ProtectedRoute>
              } />
              
              {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
              <Route path="*" element={<NotFound />} />
            </Routes>
          </BrowserRouter>
        </TooltipProvider>
      </VehicleProvider>
    </AuthProvider>
  </QueryClientProvider>
);

export default App;
