import { memo } from "react";
import { useTranslations } from "../i18n/config";

interface IChanceCardProps {
  translationKey: string,
  params: Map<string, string>,
}

const ChanceCard = ({ translationKey, params }: IChanceCardProps) => {

  const { t } = useTranslations();

  return (
    <div className="chance-card">
      {t(translationKey, params)}
    </div>
  );
}

export default memo(ChanceCard);
