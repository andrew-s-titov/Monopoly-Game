import { useStateDispatch } from "../hooks";
import { AppPage, navigate } from "../store/slice/navigation";
import { PropsWithChildren } from "react";

interface INavigatorLinkProps extends PropsWithChildren {
  to: Exclude<AppPage, 'game'>,
}

export const NavigatorLink = ({ children, to }: INavigatorLinkProps) => {

  const dispatch = useStateDispatch();
  const navigateTo = (to: AppPage) => dispatch(navigate(to));

  return (
    <div
      className="navigator-link"
      onClick={() => navigateTo(to)}
    >
      <a>{children}</a>
    </div>
  );
};
