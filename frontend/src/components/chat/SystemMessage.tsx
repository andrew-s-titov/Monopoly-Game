import { memo } from 'react';
import { Message } from "primereact/message";
import { useTranslations } from "../../i18n/config";

interface IProps {
  translationKey: string,
  params: Map<string, string>,
}

const SystemMessage = ({ translationKey, params }: IProps) => {

  const { t } = useTranslations();

  const textContent = <span>{t(translationKey, params)}</span>
  return (
    <Message
      className='chat-message system-message'
      content={<><i className='pi pi-exclamation-circle icon'/> {textContent}</>}
    />
  );
}

export default memo(SystemMessage);
