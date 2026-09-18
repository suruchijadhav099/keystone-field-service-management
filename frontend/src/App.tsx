
import React, { useEffect, useState } from "react";
import {
  BrowserRouter,
  Routes,
  Route,
  Link,
  Navigate,
  useNavigate,
} from "react-router-dom";
import { apiFetch } from "./api";

/* =========================
   TYPES
========================= */

type Role =
  | "MANAGER"
  | "DISPATCHER"
  | "TECHNICIAN"
  | "CUSTOMER";

type User = {
  id?: number;
  name?: string;
  email?: string;
  role?: Role;
};

type WorkOrder = {
  id: number;
  code: string;
  title: string;
  description?: string;
  priority?: string;
  status: string;
  slaDueAt?: string;
  photoUrl?: string;

  customer?: {
    id?: number;
    name?: string;
  };

  site?: {
    id?: number;
    name?: string;
    address?: string;
  };

  assignedTo?: {
    id?: number;
    name?: string;
    email?: string;
  };
};

type Part = {
  id: number;
  name: string;
  sku?: string;
  unitCost?: number;
  stockQty?: number;
};

type PartUsage = {
  id: number;
  quantity?: number;
  qtyUsed?: number;
  totalCost?: number;
  part?: Part;
};

type TimeLog = {
  id: number;
  minutes: number;
  note?: string;
  workOrder?: {
    id?: number;
    code?: string;
    title?: string;
  };
};

type Customer = {
  id: number;
  name: string;
  email?: string;
  phone?: string;
};

type DashboardData = {
  totalUsers?: number;
  totalCustomers?: number;
  totalJobs?: number;
  totalParts?: number;
  totalWorkOrders?: number;
  new?: number;
  assigned?: number;
  inProgress?: number;
  onHold?: number;
  completed?: number;
  closed?: number;
  cancelled?: number;
  slaTrackedWorkOrders?: number;
  overdueWorkOrders?: number;
  slaCompliantWorkOrders?: number;
  slaCompliancePercentage?: number;
  technicianBreakdown?: Record<string, number>;
  siteBreakdown?: Record<string, number>;
};

/* =========================
   USER
========================= */

function getUser(): User | null {
  const saved = localStorage.getItem("keystone_user");

  if (!saved) return null;

  try {
    return JSON.parse(saved);
  } catch {
    return null;
  }
}

function getRole(): Role | null {
  return getUser()?.role || null;
}

/* =========================
   LOGIN
========================= */

function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    setError("");
    setLoading(true);

    try {
      const response = await apiFetch("/auth/login", {
        method: "POST",
        body: JSON.stringify({
          email,
          password,
        }),
      });

      localStorage.setItem(
        "keystone_token",
        response.token
      );

      localStorage.setItem(
        "keystone_user",
        JSON.stringify({
          name: response.name,
          email,
          role: response.role,
        })
      );

      if (response.role === "MANAGER") {
        navigate("/");
      } else if (response.role === "CUSTOMER") {
        navigate("/my-portal");
      } else {
        navigate("/work-orders");
      }
    } catch (err: any) {
      setError(
        err?.message || "Invalid email or password"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>KEYSTONE</h1>

        <p>Field Service Management Platform</p>

        <form onSubmit={handleLogin}>
          <label>Email</label>

          <input
            type="email"
            placeholder="Enter email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <label>Password</label>

          <input
            type="password"
            placeholder="Enter password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <button type="submit" disabled={loading}>
            {loading ? "Logging in..." : "Login"}
          </button>
        </form>
      </div>
    </div>
  );
}

/* =========================
   LAYOUT
========================= */

function Layout({
  children,
}: {
  children: React.ReactNode;
}) {
  const navigate = useNavigate();
  const user = getUser();
  const role = user?.role;

  const logout = () => {
    localStorage.removeItem("keystone_token");
    localStorage.removeItem("keystone_user");
    navigate("/login");
  };

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <h1>KEYSTONE</h1>

        <div className="user-info">
          <span className="user-name">
            {user?.name || "User"}
          </span>

          <span className="user-role">
            {role || ""}
          </span>
        </div>

        <nav>
          {role === "MANAGER" && (
            <>
              <Link to="/">Dashboard</Link>
              <Link to="/work-orders">
                Work Orders
              </Link>
              <Link to="/customers">
                Customers
              </Link>
              <Link to="/parts">Parts</Link>
              <Link to="/time-logs">
                Time Logs
              </Link>
            </>
          )}

          {role === "DISPATCHER" && (
            <>
              <Link to="/work-orders">
                Work Orders
              </Link>
              <Link to="/customers">
                Customers
              </Link>
              <Link to="/parts">Parts</Link>
            </>
          )}

          {role === "TECHNICIAN" && (
            <>
              <Link to="/work-orders">
                Work Orders
              </Link>
              <Link to="/time-logs">
                Time Logs
              </Link>
            </>
          )}

          {role === "CUSTOMER" && (
            <>
              <Link to="/my-portal">
                My Portal
              </Link>
              <Link to="/work-orders">
                My Work Orders
              </Link>
            </>
          )}
        </nav>

        <button onClick={logout}>Logout</button>
      </aside>

      <main className="main-content">
        {children}
      </main>
    </div>
  );
}

/* =========================
   DASHBOARD
========================= */

function Dashboard() {
  const [data, setData] =
    useState<DashboardData | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const result =
          await apiFetch("/dashboard");

        setData(result);
      } catch (err: any) {
        setError(
          err?.message ||
            "Failed to load dashboard"
        );
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, []);

  if (loading) {
    return (
      <div className="loading">
        Loading dashboard...
      </div>
    );
  }

  return (
    <div>
      <h1>Dashboard</h1>

      <p className="subtitle">
        KEYSTONE Field Service Management
      </p>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      <div className="dashboard-grid">
        <div className="dashboard-card">
          <h3>Total Users</h3>
          <div className="value">
            {data?.totalUsers ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>Total Customers</h3>
          <div className="value">
            {data?.totalCustomers ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>Total Work Orders</h3>
          <div className="value">
            {data?.totalWorkOrders ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>Total Parts</h3>
          <div className="value">
            {data?.totalParts ?? 0}
          </div>
        </div>
      </div>

      <h2>Work Order Status</h2>

      <div className="dashboard-grid">
        <div className="dashboard-card">
          <h3>New</h3>
          <div className="value">
            {data?.new ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>Assigned</h3>
          <div className="value">
            {data?.assigned ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>In Progress</h3>
          <div className="value">
            {data?.inProgress ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>On Hold</h3>
          <div className="value">
            {data?.onHold ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>Completed</h3>
          <div className="value">
            {data?.completed ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>Closed</h3>
          <div className="value">
            {data?.closed ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>Cancelled</h3>
          <div className="value">
            {data?.cancelled ?? 0}
          </div>
        </div>
      </div>

      <h2>SLA</h2>

      <div className="dashboard-grid">
        <div className="dashboard-card">
          <h3>SLA Tracked</h3>
          <div className="value">
            {data?.slaTrackedWorkOrders ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>Overdue</h3>
          <div className="value">
            {data?.overdueWorkOrders ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>SLA Compliant</h3>
          <div className="value">
            {data?.slaCompliantWorkOrders ?? 0}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>SLA Compliance %</h3>
          <div className="value">
            {data?.slaCompliancePercentage ?? 0}%
          </div>
        </div>
      </div>

      <h2>Technician Breakdown</h2>

      <div className="dashboard-grid">
        {Object.entries(data?.technicianBreakdown ?? {}).map(
          ([name, count]) => (
            <div className="dashboard-card" key={name}>
              <h3>{name}</h3>
              <div className="value">{count}</div>
            </div>
          )
        )}
      </div>

      <h2>Site Breakdown</h2>

      <div className="dashboard-grid">
        {Object.entries(data?.siteBreakdown ?? {}).map(
          ([name, count]) => (
            <div className="dashboard-card" key={name}>
              <h3>{name}</h3>
              <div className="value">{count}</div>
            </div>
          )
        )}
      </div>
    </div>
  );
}

/* =========================
   WORK ORDERS
========================= */

function WorkOrders() {
  const role = getRole();

  const [orders, setOrders] =
    useState<WorkOrder[]>([]);

  const [parts, setParts] =
    useState<Part[]>([]);

  const [partUsage, setPartUsage] =
    useState<Record<number, PartUsage[]>>({});

  const [selectedPart, setSelectedPart] =
    useState<Record<number, string>>({});

  const [partQuantity, setPartQuantity] =
    useState<Record<number, number>>({});

  const [searchTerm, setSearchTerm] =
    useState("");

  const [statusFilter, setStatusFilter] =
    useState("ALL");

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const [message, setMessage] =
    useState("");

  /* =========================
     LOAD ORDERS
  ========================= */

  const loadOrders = async () => {
    try {
      setError("");

      let url = "/work-orders";

      if (
        role === "TECHNICIAN" ||
        role === "CUSTOMER"
      ) {
        url = "/work-orders/my";
      }

      const result =
        await apiFetch(url);

      setOrders(
        Array.isArray(result)
          ? result
          : []
      );
    } catch (err: any) {
      setError(
        err?.message ||
          "Failed to load work orders"
      );
    } finally {
      setLoading(false);
    }
  };

  /* =========================
     LOAD PARTS
  ========================= */

  const loadParts = async () => {
    if (
      role !== "TECHNICIAN" &&
      role !== "MANAGER" &&
      role !== "DISPATCHER"
    ) {
      return;
    }

    try {
      const result =
        await apiFetch("/parts");

      setParts(
        Array.isArray(result)
          ? result
          : []
      );
    } catch {
      // ignore
    }
  };

  /* =========================
     LOAD PART USAGE
  ========================= */

  const loadPartUsage = async (
    workOrderId: number
  ) => {
    try {
      const result =
        await apiFetch(
          `/part-usage/work-order/${workOrderId}`
        );

      setPartUsage((prev) => ({
        ...prev,
        [workOrderId]:
          Array.isArray(result)
            ? result
            : [],
      }));
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    loadOrders();
    loadParts();
  }, []);

  /* =========================
     UPDATE STATUS
  ========================= */

  const updateStatus = async (
    workOrderId: number,
    status: string
  ) => {
    setError("");
    setMessage("");

    try {
      await apiFetch(
        `/work-orders/${workOrderId}/status/${status}`,
        {
          method: "PUT",
        }
      );

      setMessage(
        `Work order status changed to ${status}.`
      );

      await loadOrders();
    } catch (err: any) {
      setError(
        err?.message ||
          "Failed to update status"
      );
    }
  };

  /* =========================
     ASSIGN TECHNICIAN
  ========================= */

  const assignTechnician = async (
    workOrderId: number,
    technicianId: number
  ) => {
    setError("");
    setMessage("");

    try {
      await apiFetch(
        `/work-orders/${workOrderId}/assign/${technicianId}`,
        {
          method: "PUT",
        }
      );

      setMessage(
        "Technician assigned successfully."
      );

      await loadOrders();
    } catch (err: any) {
      setError(
        err?.message ||
          "Failed to assign technician"
      );
    }
  };

  /* =========================
     ADD PART USAGE
  ========================= */

  const addPartUsage = async (
    workOrderId: number
  ) => {
    const partId =
      selectedPart[workOrderId];

    const quantity =
      partQuantity[workOrderId];

    setError("");
    setMessage("");

    if (!partId) {
      setError("Please select a part.");
      return;
    }

    if (!quantity || quantity <= 0) {
      setError(
        "Quantity must be greater than zero."
      );
      return;
    }

    try {
      await apiFetch(
        `/part-usage/work-order/${workOrderId}/part/${partId}?quantity=${quantity}`,
        {
          method: "POST",
        }
      );

      setMessage(
        "Part usage added successfully."
      );

      setSelectedPart((prev) => ({
        ...prev,
        [workOrderId]: "",
      }));

      setPartQuantity((prev) => ({
        ...prev,
        [workOrderId]: 1,
      }));

      await loadParts();
      await loadPartUsage(workOrderId);
    } catch (err: any) {
      setError(
        err?.message ||
          "Failed to add part usage"
      );
    }
  };

  /* =========================
     PHOTO UPLOAD
  ========================= */

  const uploadPhoto = async (
    workOrderId: number,
    file: File
  ) => {
    setError("");
    setMessage("");

    try {
      const token =
        localStorage.getItem(
          "keystone_token"
        );

      const formData = new FormData();

      formData.append("photo", file);

      const response = await fetch(
        `http://localhost:8080/api/work-orders/${workOrderId}/photo`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formData,
        }
      );

      const text =
        await response.text();

      let data: any = null;

      try {
        data = text
          ? JSON.parse(text)
          : null;
      } catch {
        data = text;
      }

      if (!response.ok) {
        throw new Error(
          data?.message ||
            data?.error ||
            `Upload failed: ${response.status}`
        );
      }

      setMessage(
        "Photo uploaded successfully."
      );

      await loadOrders();
    } catch (err: any) {
      setError(
        err?.message ||
          "Failed to upload photo"
      );
    }
  };

  /* =========================
     SEARCH + FILTER
  ========================= */

  const filteredOrders =
    orders.filter((order) => {
      const search =
        searchTerm
          .toLowerCase()
          .trim();

      const matchesSearch =
        !search ||
        order.code
          .toLowerCase()
          .includes(search) ||
        order.title
          .toLowerCase()
          .includes(search) ||
        (
          order.customer?.name || ""
        )
          .toLowerCase()
          .includes(search);

      const matchesStatus =
        statusFilter === "ALL" ||
        order.status === statusFilter;

      return (
        matchesSearch &&
        matchesStatus
      );
    });

  /* =========================
     LOADING
  ========================= */

  if (loading) {
    return (
      <div className="loading">
        Loading work orders...
      </div>
    );
  }

  return (
    <div>
      <h1>
        {role === "CUSTOMER"
          ? "My Work Orders"
          : "Work Orders"}
      </h1>

      <p className="subtitle">
        Manage work orders and service activities
      </p>

      {/* =========================
          SEARCH + FILTER
      ========================= */}

      <div
        style={{
          display: "flex",
          gap: "15px",
          marginBottom: "25px",
          flexWrap: "wrap",
        }}
      >
        <input
          type="text"
          placeholder="Search code, title or customer..."
          value={searchTerm}
          onChange={(e) =>
            setSearchTerm(e.target.value)
          }
          style={{
            flex: 1,
            minWidth: "250px",
            padding: "12px 15px",
            border: "1px solid #ccc",
            borderRadius: "8px",
            fontSize: "16px",
          }}
        />

        <select
          value={statusFilter}
          onChange={(e) =>
            setStatusFilter(e.target.value)
          }
          style={{
            minWidth: "180px",
            padding: "12px 15px",
            border: "1px solid #ccc",
            borderRadius: "8px",
            fontSize: "16px",
          }}
        >
          <option value="ALL">
            All Statuses
          </option>

          <option value="NEW">
            NEW
          </option>

          <option value="ASSIGNED">
            ASSIGNED
          </option>

          <option value="IN_PROGRESS">
            IN_PROGRESS
          </option>

          <option value="ON_HOLD">
            ON_HOLD
          </option>

          <option value="COMPLETED">
            COMPLETED
          </option>

          <option value="CLOSED">
            CLOSED
          </option>

          <option value="CANCELLED">
            CANCELLED
          </option>
        </select>
      </div>

      <p
        style={{
          marginBottom: "20px",
          color: "#666",
        }}
      >
        Showing{" "}
        <strong>
          {filteredOrders.length}
        </strong>{" "}
        of{" "}
        <strong>
          {orders.length}
        </strong>{" "}
        work orders
      </p>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {message && (
        <div className="success-message">
          {message}
        </div>
      )}

      {/* =========================
          WORK ORDER LIST
      ========================= */}

      <div className="work-order-list">
        {filteredOrders.length === 0 && (
          <div className="portal-card">
            No work orders match your search or filter.
          </div>
        )}

        {filteredOrders.map((order) => {
          const usage =
            partUsage[order.id] || [];

          return (
            <div
              className="work-order-card"
              key={order.id}
            >
              <h2>{order.code}</h2>

              <h3>{order.title}</h3>

              <div className="status">
                {order.status}
              </div>

              <p>
                <strong>
                  Description:
                </strong>{" "}
                {order.description || "—"}
              </p>

              <p>
                <strong>
                  Priority:
                </strong>{" "}
                {order.priority || "—"}
              </p>

              <p>
                <strong>
                  Customer:
                </strong>{" "}
                {order.customer?.name || "—"}
              </p>

              <p>
                <strong>
                  Site:
                </strong>{" "}
                {order.site?.name || "—"}
              </p>

              <p>
                <strong>
                  Assigned Technician:
                </strong>{" "}
                {order.assignedTo?.name ||
                  "Not Assigned"}
              </p>

              <p>
                <strong>
                  SLA Due:
                </strong>{" "}
                {order.slaDueAt || "—"}
              </p>

              {/* =========================
                  MANAGER / DISPATCHER
              ========================= */}

              {(role === "MANAGER" ||
                role === "DISPATCHER") && (
                <div className="work-order-actions">
                  <h3>
                    Assign Technician
                  </h3>

                  <button
                    className="secondary-btn"
                    onClick={() =>
                      assignTechnician(
                        order.id,
                        2
                      )
                    }
                  >
                    Assign Technician 2
                  </button>

                  <button
                    className="secondary-btn"
                    onClick={() =>
                      assignTechnician(
                        order.id,
                        4
                      )
                    }
                  >
                    Assign Technician 4
                  </button>

                  <button
                    className="secondary-btn"
                    onClick={() =>
                      assignTechnician(
                        order.id,
                        13
                      )
                    }
                  >
                    Assign Photo Technician
                  </button>
                </div>
              )}

              {/* =========================
                  TECHNICIAN ACTIONS
              ========================= */}

              {role === "TECHNICIAN" && (
                <div className="work-order-actions">
                  <h3>
                    Work Order Actions
                  </h3>

                  {order.status ===
                    "ASSIGNED" && (
                    <button
                      className="primary-btn"
                      onClick={() =>
                        updateStatus(
                          order.id,
                          "IN_PROGRESS"
                        )
                      }
                    >
                      Start
                    </button>
                  )}

                  {order.status ===
                    "IN_PROGRESS" && (
                    <>
                      <button
                        className="secondary-btn"
                        onClick={() =>
                          updateStatus(
                            order.id,
                            "ON_HOLD"
                          )
                        }
                      >
                        Hold
                      </button>

                      <button
                        className="success-btn"
                        onClick={() =>
                          updateStatus(
                            order.id,
                            "COMPLETED"
                          )
                        }
                      >
                        Complete
                      </button>
                    </>
                  )}

                  {order.status ===
                    "ON_HOLD" && (
                    <button
                      className="primary-btn"
                      onClick={() =>
                        updateStatus(
                          order.id,
                          "IN_PROGRESS"
                        )
                      }
                    >
                      Resume
                    </button>
                  )}

                  {/* =========================
                      PHOTO UPLOAD
                  ========================= */}

                  {order.status !==
                    "CLOSED" &&
                    order.status !==
                      "CANCELLED" && (
                    <div
                      style={{
                        marginTop: "20px",
                        padding: "15px",
                        border:
                          "1px solid #ddd",
                        borderRadius: "8px",
                      }}
                    >
                      <h3>
                        Upload Service Photo
                      </h3>

                      <input
                        type="file"
                        accept="image/*"
                        onChange={(e) => {
                          const file =
                            e.target.files?.[0];

                          if (file) {
                            uploadPhoto(
                              order.id,
                              file
                            );
                          }
                        }}
                      />

                      {order.photoUrl && (
                        <div
                          style={{
                            marginTop: "15px",
                          }}
                        >
                          <p>
                            <strong>
                              Uploaded Photo:
                            </strong>
                          </p>

                          <img
                            src={`http://localhost:8080${order.photoUrl}`}
                            alt="Work order service"
                            style={{
                              maxWidth: "300px",
                              maxHeight: "200px",
                              borderRadius: "8px",
                              objectFit: "cover",
                            }}
                          />
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}

              {/* =========================
                  PARTS
              ========================= */}

              {(role === "TECHNICIAN" ||
                role === "MANAGER") && (
                <div className="parts-section">
                  <h3>
                    Parts Used
                  </h3>

                  <button
                    className="secondary-btn"
                    onClick={() =>
                      loadPartUsage(
                        order.id
                      )
                    }
                  >
                    View Parts
                  </button>

                  {usage.length > 0 && (
                    <div
                      className="table-container"
                      style={{
                        marginTop: "15px",
                      }}
                    >
                      <table>
                        <thead>
                          <tr>
                            <th>
                              Part
                            </th>
                            <th>
                              Quantity
                            </th>
                            <th>
                              Total Cost
                            </th>
                          </tr>
                        </thead>

                        <tbody>
                          {usage.map(
                            (item) => (
                              <tr
                                key={
                                  item.id
                                }
                              >
                                <td>
                                  {item.part
                                    ?.name ||
                                    "—"}
                                </td>

                                <td>
                                  {item.qtyUsed ??
                                    item.quantity ??
                                    "—"}
                                </td>

                                <td>
                                  ₹
                                  {item.totalCost ??
                                    0}
                                </td>
                              </tr>
                            )
                          )}
                        </tbody>
                      </table>
                    </div>
                  )}

                  {role ===
                    "TECHNICIAN" &&
                    order.status !==
                      "CLOSED" &&
                    order.status !==
                      "CANCELLED" && (
                    <div className="part-usage-form">
                      <select
                        value={
                          selectedPart[
                            order.id
                          ] || ""
                        }
                        onChange={(e) =>
                          setSelectedPart(
                            (prev) => ({
                              ...prev,
                              [order.id]:
                                e.target.value,
                            })
                          )
                        }
                      >
                        <option value="">
                          Select Part
                        </option>

                        {parts.map(
                          (part) => (
                            <option
                              key={
                                part.id
                              }
                              value={
                                part.id
                              }
                            >
                              {part.name}
                              {" — Stock: "}
                              {part.stockQty ??
                                0}
                            </option>
                          )
                        )}
                      </select>

                      <input
                        type="number"
                        min="1"
                        value={
                          partQuantity[
                            order.id
                          ] ?? 1
                        }
                        onChange={(e) =>
                          setPartQuantity(
                            (prev) => ({
                              ...prev,
                              [order.id]:
                                Number(
                                  e.target
                                    .value
                                ),
                            })
                          )
                        }
                      />

                      <button
                        className="primary-btn"
                        onClick={() =>
                          addPartUsage(
                            order.id
                          )
                        }
                      >
                        Add Part
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

/* =========================
   CUSTOMERS
========================= */

function Customers() {
  const [customers, setCustomers] =
    useState<Customer[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  useEffect(() => {
    const loadCustomers = async () => {
      try {
        const result =
          await apiFetch("/customers");

        setCustomers(
          Array.isArray(result)
            ? result
            : []
        );
      } catch (err: any) {
        setError(
          err?.message ||
            "Failed to load customers"
        );
      } finally {
        setLoading(false);
      }
    };

    loadCustomers();
  }, []);

  if (loading) {
    return (
      <div className="loading">
        Loading customers...
      </div>
    );
  }

  return (
    <div>
      <h1>Customers</h1>

      <p className="subtitle">
        Customer management
      </p>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Phone</th>
            </tr>
          </thead>

          <tbody>
            {customers.map(
              (customer) => (
                <tr key={customer.id}>
                  <td>
                    {customer.id}
                  </td>

                  <td>
                    {customer.name}
                  </td>

                  <td>
                    {customer.email ||
                      "—"}
                  </td>

                  <td>
                    {customer.phone ||
                      "—"}
                  </td>
                </tr>
              )
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

/* =========================
   PARTS
========================= */

function Parts() {
  const [parts, setParts] =
    useState<Part[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  useEffect(() => {
    const loadParts = async () => {
      try {
        const result =
          await apiFetch("/parts");

        setParts(
          Array.isArray(result)
            ? result
            : []
        );
      } catch (err: any) {
        setError(
          err?.message ||
            "Failed to load parts"
        );
      } finally {
        setLoading(false);
      }
    };

    loadParts();
  }, []);

  if (loading) {
    return (
      <div className="loading">
        Loading parts...
      </div>
    );
  }

  return (
    <div>
      <h1>Parts</h1>

      <p className="subtitle">
        Parts and inventory management
      </p>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>SKU</th>
              <th>Unit Cost</th>
              <th>Stock</th>
            </tr>
          </thead>

          <tbody>
            {parts.map((part) => (
              <tr key={part.id}>
                <td>{part.id}</td>
                <td>{part.name}</td>
                <td>
                  {part.sku || "—"}
                </td>
                <td>
                  ₹{part.unitCost ?? 0}
                </td>
                <td>
                  {part.stockQty ?? 0}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

/* =========================
   TIME LOGS
========================= */

function TimeLogs() {
  const [orders, setOrders] =
    useState<WorkOrder[]>([]);

  const [logs, setLogs] =
    useState<TimeLog[]>([]);

  const [workOrderId, setWorkOrderId] =
    useState("");

  const [minutes, setMinutes] =
    useState("");

  const [note, setNote] =
    useState("");

  const [message, setMessage] =
    useState("");

  const [error, setError] =
    useState("");

  const [loading, setLoading] =
    useState(true);

  const loadData = async () => {
    try {
      const orderResult =
        await apiFetch(
          "/work-orders/my"
        );

      setOrders(
        Array.isArray(orderResult)
          ? orderResult
          : []
      );

      const logResult =
        await apiFetch(
          "/time-logs/my"
        );

      setLogs(
        Array.isArray(logResult)
          ? logResult
          : []
      );
    } catch (err: any) {
      setError(
        err?.message ||
          "Failed to load time logs"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const addTimeLog = async (
    e: React.FormEvent
  ) => {
    e.preventDefault();

    setError("");
    setMessage("");

    if (!workOrderId) {
      setError(
        "Please select a work order."
      );
      return;
    }

    if (
      !minutes ||
      Number(minutes) <= 0
    ) {
      setError(
        "Minutes must be greater than zero."
      );
      return;
    }

    try {
      await apiFetch("/time-logs", {
        method: "POST",
        body: JSON.stringify({
          workOrder: {
            id: Number(workOrderId),
          },
          minutes: Number(minutes),
          note,
        }),
      });

      setMessage(
        "Time log added successfully."
      );

      setWorkOrderId("");
      setMinutes("");
      setNote("");

      await loadData();
    } catch (err: any) {
      setError(
        err?.message ||
          "Failed to add time log"
      );
    }
  };

  if (loading) {
    return (
      <div className="loading">
        Loading time logs...
      </div>
    );
  }

  return (
    <div>
      <h1>Time Logs</h1>

      <p className="subtitle">
        Track technician working time
      </p>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {message && (
        <div className="success-message">
          {message}
        </div>
      )}

      <h2>Add Time Log</h2>

      <form
        className="time-log-form"
        onSubmit={addTimeLog}
      >
        <select
          value={workOrderId}
          onChange={(e) =>
            setWorkOrderId(
              e.target.value
            )
          }
          required
        >
          <option value="">
            Select Work Order
          </option>

          {orders.map((order) => (
            <option
              key={order.id}
              value={order.id}
            >
              {order.code} -{" "}
              {order.title}
            </option>
          ))}
        </select>

        <input
          type="number"
          min="1"
          placeholder="Minutes"
          value={minutes}
          onChange={(e) =>
            setMinutes(e.target.value)
          }
          required
        />

        <textarea
          placeholder="Work note"
          value={note}
          onChange={(e) =>
            setNote(e.target.value)
          }
        />

        <button type="submit">
          Add Time Log
        </button>
      </form>

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>Work Order</th>
              <th>Minutes</th>
              <th>Note</th>
            </tr>
          </thead>

          <tbody>
            {logs.map((log) => (
              <tr key={log.id}>
                <td>
                  {log.workOrder?.code ||
                    "—"}
                </td>

                <td>
                  {log.minutes}
                </td>

                <td>
                  {log.note || "—"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

/* =========================
   CUSTOMER PORTAL
========================= */

function CustomerPortal() {
  const [orders, setOrders] =
    useState<WorkOrder[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  useEffect(() => {
    const loadOrders = async () => {
      try {
        const result =
          await apiFetch(
            "/work-orders/my"
          );

        setOrders(
          Array.isArray(result)
            ? result
            : []
        );
      } catch (err: any) {
        setError(
          err?.message ||
            "Failed to load your work orders"
        );
      } finally {
        setLoading(false);
      }
    };

    loadOrders();
  }, []);

  if (loading) {
    return (
      <div className="loading">
        Loading your portal...
      </div>
    );
  }

  return (
    <div>
      <h1>My Portal</h1>

      <p className="subtitle">
        View your service requests and work orders
      </p>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {orders.length === 0 && (
        <div className="portal-card">
          No work orders found.
        </div>
      )}

      {orders.map((order) => (
        <div
          className="portal-card"
          key={order.id}
        >
          <h2>{order.code}</h2>

          <h3>{order.title}</h3>

          <div className="status">
            {order.status}
          </div>

          <p>
            <strong>
              Description:
            </strong>{" "}
            {order.description || "—"}
          </p>

          <p>
            <strong>
              Priority:
            </strong>{" "}
            {order.priority || "—"}
          </p>

          <p>
            <strong>
              Site:
            </strong>{" "}
            {order.site?.name || "—"}
          </p>

          <p>
            <strong>
              SLA Due:
            </strong>{" "}
            {order.slaDueAt || "—"}
          </p>
        </div>
      ))}
    </div>
  );
}

/* =========================
   PROTECTED ROUTE
========================= */

function ProtectedRoute({
  children,
}: {
  children: React.ReactNode;
}) {
  const token =
    localStorage.getItem(
      "keystone_token"
    );

  if (!token) {
    return (
      <Navigate
        to="/login"
        replace
      />
    );
  }

  return (
    <Layout>
      {children}
    </Layout>
  );
}

/* =========================
   APP
========================= */

function App() {
  const role = getRole();

  return (
    <BrowserRouter>
      <Routes>

        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/"
          element={
            <ProtectedRoute>
              {role === "MANAGER" ? (
                <Dashboard />
              ) : (
                <Navigate
                  to="/work-orders"
                  replace
                />
              )}
            </ProtectedRoute>
          }
        />

        <Route
          path="/work-orders"
          element={
            <ProtectedRoute>
              <WorkOrders />
            </ProtectedRoute>
          }
        />

        <Route
          path="/customers"
          element={
            <ProtectedRoute>
              <Customers />
            </ProtectedRoute>
          }
        />

        <Route
          path="/parts"
          element={
            <ProtectedRoute>
              <Parts />
            </ProtectedRoute>
          }
        />

        <Route
          path="/time-logs"
          element={
            <ProtectedRoute>
              <TimeLogs />
            </ProtectedRoute>
          }
        />

        <Route
          path="/my-portal"
          element={
            <ProtectedRoute>
              <CustomerPortal />
            </ProtectedRoute>
          }
        />

        <Route
          path="*"
          element={
            <Navigate
              to={
                role === "MANAGER"
                  ? "/"
                  : role === "CUSTOMER"
                  ? "/my-portal"
                  : "/work-orders"
              }
              replace
            />
          }
        />

      </Routes>
    </BrowserRouter>
  );
}

export default App;
