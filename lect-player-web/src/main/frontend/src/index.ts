import './style.css';

export * from './extension';
export * from './component';
export * from './view';
export * from './virtual-scroller/virtual-scroller';

import { WebWindowView } from './view/window-view/web-window.view';
import { WindowPresenter } from './api/presenter/window.presenter';

const windowView = new WebWindowView();
const windowPresenter = new WindowPresenter(windowView);

windowPresenter.initialize();
windowPresenter.openRecording("${recordingFile}");