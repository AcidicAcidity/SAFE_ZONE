import { useAuth } from "../net/auth";
import { NavigationCard, PageHeader } from "./components/common";
import { Icon } from "./components/Icon";

export function HomePage() {
  const { user } = useAuth();
  return (
    <>
      <div className="home-heading">
        <span className="eyebrow">Личный кабинет</span>
        <h1>Главная</h1>
        <p>Всё, что нужно для вашего хранения.</p>
      </div>
      <div className="section-label">Выберите действие</div>
      <div className="navigation-grid">
        <NavigationCard
          title="Склады"
          description="Адреса складов и места для хранения."
          to="/warehouses"
          icon="warehouse"
        />
        <NavigationCard
          title="Все ячейки"
          description="Размеры, расположение и доступность ячеек."
          to="/cells"
          icon="grid"
        />
        <NavigationCard
          title="Арендовать"
          description="Выбор ячейки или размера и срока аренды."
          to="/rent"
          icon="key"
        />
        <NavigationCard
          title="Мои аренды"
          description="Ваши бронирования и сроки хранения."
          to="/rentals"
          icon="calendar"
        />
      </div>
      {user?.role === "ADMIN" && (
        <div className="admin-entry">
          <NavigationCard
            title="Панель администратора"
            description="Статистика, аккаунты и управление бронированиями."
            to="/admin"
            icon="shield"
            accent
          />
        </div>
      )}
      <div className="home-note">
        <Icon name="info" />
        <p>Начните со списка складов или просмотрите доступные ячейки.</p>
      </div>
    </>
  );
}
export function AdminPage() {
  return (
    <>
      <PageHeader
        title="Панель администратора"
        description="Управление аккаунтами и обзор бронирований."
      />
      <div className="navigation-grid">
        <NavigationCard
          title="Статистика по ячейкам"
          description="Количество аренд каждой ячейки."
          to="/admin/statistics"
          icon="chart"
        />
        <NavigationCard
          title="Управление аккаунтами"
          description="Пользователи, роли и статусы аккаунтов."
          to="/admin/users"
          icon="users"
        />
        <NavigationCard
          title="Управление бронями"
          description="Все бронирования и сроки аренды."
          to="/admin/rentals"
          icon="calendar"
        />
        <NavigationCard
          title="Экспорт базы в XLSX"
          description="Выгрузка базы пока недоступна."
          icon="download"
          disabled
        />
      </div>
    </>
  );
}
