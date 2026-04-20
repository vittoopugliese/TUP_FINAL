/**
 * Slide 15
 */
export const meta = {
  index: 15,
  title: "Demostración — Alex (2)",
};

export const styles = `
  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  body {
    margin: 0;
    padding: 0;
    overflow: hidden;
    font-family: 'Roboto', sans-serif;
    background: var(--color-body-bg);
  }

  .slide-container {
    width: 1280px;
    height: 720px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, var(--slide-bg-1) 0%, var(--slide-bg-2) 50%, var(--slide-bg-3) 100%);
    color: white;
  }

  .content-wrapper {
    width: 1160px;
    height: 620px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 24px;
  }

  .main-title {
    font-family: 'Montserrat', sans-serif;
    font-size: 36px;
    font-weight: 700;
    text-align: center;
  }

  .video-frame {
    width: 100%;
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 12px;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.12);
    box-shadow: 0 24px 60px rgba(0, 0, 0, 0.28);
  }

  .demo-video {
    width: 100%;
    height: 100%;
    object-fit: contain;
    border-radius: 16px;
    background: #000;
  }
`;

export const html = `
  <div class="slide-container">
    <div class="content-wrapper">
      <h1 class="main-title">Firmar Inspección y generar Reporte PDF</h1>
      <div class="video-frame">
        <video class="demo-video" autoplay loop playsinline>
          <source src="assets/alex5.mp4" type="video/mp4" />
        </video>
      </div>
    </div>
  </div>
`;
