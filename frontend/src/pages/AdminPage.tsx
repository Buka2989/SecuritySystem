import { useEffect, useState, type FormEvent } from 'react';
import { useAuth } from '@/hooks/useAuth';
import {
  getVerificationRequests,
  approveVerificationRequest,
  rejectVerificationRequest,
  type VerificationRequest,
} from '@/services/api/adminService';

const relationshipLabels: Record<string, string> = {
  FATHER: 'Отец',
  MOTHER: 'Мать',
  GRANDFATHER: 'Дедушка',
  GRANDMOTHER: 'Бабушка',
  GUARDIAN: 'Опекун',
};

const statusLabels: Record<string, string> = {
  PENDING: 'На рассмотрении',
  APPROVED: 'Одобрено',
  REJECTED: 'Отклонено',
  CANCELLED: 'Отменено',
};

export function AdminPage() {
  const { token, user } = useAuth();

  const [requests, setRequests] = useState<VerificationRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [processingId, setProcessingId] = useState<string | null>(null);
  const [rejection, setRejection] = useState<{ id: string; comment: string } | null>(null);

  useEffect(() => {
    async function loadRequests() {
      try {
        setLoading(true);
        setError(null);

        const data = await getVerificationRequests(token);
        setRequests(data);
      } catch (err) {
        console.error(err);
        setError('Не удалось загрузить заявки.');
      } finally {
        setLoading(false);
      }
    }

    loadRequests();
  }, [token]);

  async function handleApprove(requestId: string) {
    try {
      setProcessingId(requestId);
      setError(null);

      await approveVerificationRequest(requestId, token);

      const data = await getVerificationRequests(token);
      setRequests(data);
    } catch (err) {
      console.error(err);
      setError('Не удалось одобрить заявку.');
    } finally {
      setProcessingId(null);
    }
  }

  async function handleReject(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!rejection || !rejection.comment.trim() || processingId) return;
    const { id: requestId, comment } = rejection;

    try {
      setProcessingId(requestId);
      setError(null);

      await rejectVerificationRequest(
        requestId,
        comment.trim(),
        token,
      );

      const data = await getVerificationRequests(token);
      setRequests(data);
      setRejection(null);
    } catch (err) {
      console.error(err);
      setError('Не удалось отклонить заявку.');
    } finally {
      setProcessingId(null);
    }
  }

  const pendingRequests = requests.filter(
    (request) => request.status === 'PENDING',
  );

  const approvedCount = requests.filter(
    (request) => request.status === 'APPROVED',
  ).length;

  const rejectedCount = requests.filter(
    (request) => request.status === 'REJECTED',
  ).length;

  const processedRequests = requests.filter(
    (request) => request.status !== 'PENDING',
  );

  return (
    <main className="page">
      {/* Header */}
      <div className="admin-header">
        <div>
          <h1 className="admin-title">
            Панель администратора
          </h1>

          <p className="admin-subtitle">
            Управление заявками на подтверждение
          </p>
        </div>

        <div className="admin-welcome">
          {user?.fullName}
        </div>
      </div>

      {/* Statistics */}
      <div className="admin-stats">
        <div className="admin-stat-card">
          <span className="admin-stat-card__label">
            На рассмотрении
          </span>

          <strong className="admin-stat-card__value">
            {pendingRequests.length}
          </strong>
        </div>

        <div className="admin-stat-card">
          <span className="admin-stat-card__label">
            Одобрено
          </span>

          <strong className="admin-stat-card__value">
            {approvedCount}
          </strong>
        </div>

        <div className="admin-stat-card">
          <span className="admin-stat-card__label">
            Отклонено
          </span>

          <strong className="admin-stat-card__value">
            {rejectedCount}
          </strong>
        </div>
      </div>

      {/* Loading */}
      {loading && (
        <p className="page__subtitle">
          Загрузка заявок...
        </p>
      )}

      {/* Error */}
      {error && (
        <div className="form-error">
          {error}
        </div>
      )}

      {!loading && !error && (
        <>
          {/* Pending requests */}
          <section className="admin-section">
            <div className="admin-section__header">
              <div>
                <h2>Заявки на рассмотрении</h2>

                <p>
                  Заявки, которые требуют решения администратора.
                </p>
              </div>

              <span className="admin-section__count">
                {pendingRequests.length}
              </span>
            </div>

            {pendingRequests.length === 0 ? (
              <div className="admin-empty">
                <h3>Новых заявок нет</h3>

                <p>
                  Все заявки были обработаны.
                </p>
              </div>
            ) : (
              <div className="admin-requests">
                {pendingRequests.map((request) => (
                  <article
                    key={request.id}
                    className="admin-request-card"
                  >
                    <div className="admin-request-card__header">
                      <div>
                        <h3>
                          {request.student_name}
                        </h3>

                        <p className="admin-request-card__class">
                          {request.class_name}
                        </p>
                      </div>

                      <span className="status-badge status-badge--pending">
                        {statusLabels[request.status]}
                      </span>
                    </div>

                    <div className="admin-request-card__body">
                      <div className="admin-info">
                        <span className="admin-info__label">
                          Заявитель
                        </span>

                        <strong>
                          {request.guardian_name}
                        </strong>
                      </div>

                      <div className="admin-info">
                        <span className="admin-info__label">
                          Кем приходится
                        </span>

                        <strong>
                          {relationshipLabels[
                            request.relationship_type
                          ] ?? request.relationship_type}
                        </strong>
                      </div>

                      <div className="admin-info">
                        <span className="admin-info__label">
                          Дата заявки
                        </span>

                        <strong>
                          {new Date(
                            request.created_at,
                          ).toLocaleString('ru-RU')}
                        </strong>
                      </div>
                    </div>

                    <div className="admin-request-card__actions">
                      <button
                        className="btn-primary"
                        onClick={() =>
                          handleApprove(request.id)
                        }
                        disabled={
                          processingId !== null || rejection?.id === request.id
                        }
                      >
                        {processingId === request.id
                          ? 'Обработка…'
                          : 'Одобрить'}
                      </button>

                      <button
                        className="btn-secondary"
                        onClick={() => setRejection({ id: request.id, comment: '' })}
                        disabled={
                          processingId !== null || rejection?.id === request.id
                        }
                      >
                        Отклонить
                      </button>
                    </div>
                    {rejection?.id === request.id && (
                      <form className="admin-rejection-form" onSubmit={handleReject}>
                        <div className="form-group">
                          <label htmlFor={`rejection-${request.id}`}>Причина отклонения</label>
                          <textarea
                            id={`rejection-${request.id}`}
                            rows={3}
                            required
                            autoFocus
                            value={rejection.comment}
                            onChange={(event) => setRejection({ id: request.id, comment: event.target.value })}
                            disabled={processingId !== null}
                          />
                        </div>
                        <div className="admin-request-card__actions">
                          <button type="submit" className="btn-primary" disabled={processingId !== null || !rejection.comment.trim()}>
                            Подтвердить отклонение
                          </button>
                          <button type="button" className="btn-secondary" disabled={processingId !== null} onClick={() => setRejection(null)}>
                            Отмена
                          </button>
                        </div>
                      </form>
                    )}
                  </article>
                ))}
              </div>
            )}
          </section>

          {/* History */}
          <section className="admin-section">
            <div className="admin-section__header">
              <div>
                <h2>История заявок</h2>

                <p>
                  Уже обработанные заявки.
                </p>
              </div>

              <span className="admin-section__count">
                {processedRequests.length}
              </span>
            </div>

            {processedRequests.length === 0 ? (
              <div className="admin-empty">
                <h3>История пуста</h3>

                <p>
                  Обработанных заявок пока нет.
                </p>
              </div>
            ) : (
              <div className="admin-requests">
                {processedRequests.map((request) => (
                  <article
                    key={request.id}
                    className="admin-request-card"
                  >
                    <div className="admin-request-card__header">
                      <div>
                        <h3>
                          {request.student_name}
                        </h3>

                        <p className="admin-request-card__class">
                          {request.class_name}
                        </p>
                      </div>

                      <span
                        className={`status-badge status-badge--${request.status.toLowerCase()}`}
                      >
                        {statusLabels[request.status]}
                      </span>
                    </div>

                    <div className="admin-request-card__body">
                      <div className="admin-info">
                        <span className="admin-info__label">
                          Заявитель
                        </span>

                        <strong>
                          {request.guardian_name}
                        </strong>
                      </div>

                      <div className="admin-info">
                        <span className="admin-info__label">
                          Кем приходится
                        </span>

                        <strong>
                          {relationshipLabels[
                            request.relationship_type
                          ] ?? request.relationship_type}
                        </strong>
                      </div>

                      <div className="admin-info">
                        <span className="admin-info__label">
                          Дата заявки
                        </span>

                        <strong>
                          {new Date(
                            request.created_at,
                          ).toLocaleString('ru-RU')}
                        </strong>
                      </div>

                      {request.comment && (
                        <div className="admin-info">
                          <span className="admin-info__label">
                            Комментарий
                          </span>

                          <strong>
                            {request.comment}
                          </strong>
                        </div>
                      )}
                    </div>
                  </article>
                ))}
              </div>
            )}
          </section>
        </>
      )}
    </main>
  );
}
