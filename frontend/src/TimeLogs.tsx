import { useEffect, useState } from "react";
import { api } from "./api";

type WorkOrder = {
  id: number;
  code: string;
  title: string;
  status: string;
};

type TimeLog = {
  id: number;
  minutes: number;
  note: string;
  workOrder?: {
    id: number;
    code: string;
  };
};

function TimeLogs() {
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [items, setItems] = useState<TimeLog[]>([]);

  const [workOrderId, setWorkOrderId] = useState("");
  const [minutes, setMinutes] = useState("");
  const [note, setNote] = useState("");

  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  /* =========================
     LOAD ASSIGNED WORK ORDERS
  ========================= */

  async function loadWorkOrders() {
    try {
      const result = await api("/api/work-orders/technician/4");

      if (Array.isArray(result)) {
        setWorkOrders(result);
      } else {
        setWorkOrders([]);
      }
    } catch (e: any) {
      setError(e.message || "Unable to load work orders");
    }
  }

  /* =========================
     LOAD MY TIME LOGS
  ========================= */

  async function loadTimeLogs() {
    try {
      const result = await api("/api/time-logs/my");

      if (Array.isArray(result)) {
        setItems(result);
      } else {
        setItems([]);
      }
    } catch (e: any) {
      setError(e.message || "Unable to load time logs");
    }
  }

  /* =========================
     INITIAL LOAD
  ========================= */

  useEffect(() => {
    loadWorkOrders();
    loadTimeLogs();
  }, []);

  /* =========================
     ADD TIME LOG
  ========================= */

  async function addTimeLog(e: React.FormEvent) {
    e.preventDefault();

    setError("");
    setMessage("");

    if (!workOrderId) {
      setError("Please select a work order");
      return;
    }

    if (!minutes || Number(minutes) <= 0) {
      setError("Minutes must be greater than zero");
      return;
    }

    try {
      setLoading(true);

      await api("/api/time-logs", {
        method: "POST",
        body: JSON.stringify({
          workOrder: {
            id: Number(workOrderId)
          },
          minutes: Number(minutes),
          note: note
        })
      });

      setMessage("Time log added successfully");

      setWorkOrderId("");
      setMinutes("");
      setNote("");

      await loadTimeLogs();
      await loadWorkOrders();

    } catch (e: any) {
      setError(e.message || "Unable to add time log");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">

      {/* =========================
          HEADER
      ========================= */}

      <div className="page-header">
        <div>
          <h1>My Time Logs</h1>
          <p>KEYSTONE Field Service Management</p>
        </div>
      </div>

      {/* =========================
          ERROR
      ========================= */}

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {/* =========================
          SUCCESS
      ========================= */}

      {message && (
        <div className="success-message">
          {message}
        </div>
      )}

      {/* =========================
          ADD TIME LOG
      ========================= */}

      <div className="card">

        <h2>Add Time Log</h2>

        <form onSubmit={addTimeLog}>

          <label>
            Work Order
          </label>

          <select
            value={workOrderId}
            onChange={(e) => setWorkOrderId(e.target.value)}
          >

            <option value="">
              Select Work Order
            </option>

            {workOrders.map((workOrder) => (
              <option
                key={workOrder.id}
                value={workOrder.id}
              >
                {workOrder.code} - {workOrder.title}
              </option>
            ))}

          </select>


          <label>
            Minutes
          </label>

          <input
            type="number"
            min="1"
            placeholder="Example: 60"
            value={minutes}
            onChange={(e) => setMinutes(e.target.value)}
          />


          <label>
            Work Note
          </label>

          <textarea
            placeholder="Describe the work performed..."
            value={note}
            onChange={(e) => setNote(e.target.value)}
          />


          <button
            type="submit"
            className="primary-button"
            disabled={loading}
          >
            {loading ? "Saving..." : "Add Time Log"}
          </button>

        </form>

      </div>


      {/* =========================
          MY TIME LOGS
      ========================= */}

      <div className="card">

        <h2>My Time Logs</h2>

        <div className="table-wrap">

          <table>

            <thead>

              <tr>
                <th>ID</th>
                <th>Work Order</th>
                <th>Minutes</th>
                <th>Note</th>
              </tr>

            </thead>

            <tbody>

              {items.length === 0 ? (

                <tr>
                  <td colSpan={4}>
                    No time logs found
                  </td>
                </tr>

              ) : (

                items.map((item) => (

                  <tr key={item.id}>

                    <td>
                      {item.id}
                    </td>

                    <td>
                      {item.workOrder?.code || "-"}
                    </td>

                    <td>
                      {item.minutes}
                    </td>

                    <td>
                      {item.note || "-"}
                    </td>

                  </tr>

                ))

              )}

            </tbody>

          </table>

        </div>

      </div>

    </div>
  );
}

export default TimeLogs;