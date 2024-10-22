import java.util.Arrays;
import static java.lang.Math.min;

public class matrixproduct {

    public static void OnMult(int m_ar, int m_br) {
        long time1, time2;

        double[] pha = new double[m_ar*m_ar];
        double[] phb = new double[m_ar*m_br];
        double[] phc = new double[m_ar*m_br];

        for (int i = 0; i < m_ar; i++)
            for (int j = 0; j < m_ar; j++)
                pha[i*m_ar+j] = (double)1.0;

        for (int i = 0; i < m_ar; i++)
            for (int j = 0; j < m_br; j++)
                phb[i*m_br+j] = (double)(i + 1);

        time1 = System.currentTimeMillis();

        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_br; j++) {
                double temp = 0;
                for (int k = 0; k < m_ar; k++) {
                    temp += pha[i*m_ar+k] * phb[k*m_br+j];
                }
                phc[i*m_ar+j] = temp;
            }
        }

        time2 = System.currentTimeMillis();
        double seconds = (time2 - time1) / 1000.0;

        System.out.printf("Time: %.3f seconds\n", seconds);

        System.out.println("Result matrix: ");
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < Math.min(10, m_br); j++) {
                System.out.print(phc[j] + " ");
            }
        }
        System.out.println();

    }

    public static void OnMultLine(int m_ar, int m_br) {
        long time1, time2;

        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_br * m_br];
        double[] phc = new double[m_ar * m_ar];

        for (int i = 0; i < m_ar; i++)
            for (int j = 0; j < m_ar; j++)
                pha[i * m_ar + j] = 1.0;

        for (int i = 0; i < m_ar; i++)
            for (int j = 0; j < m_br; j++)
                phb[i * m_br + j] = i + 1;

        time1 = System.currentTimeMillis();

        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_ar; j++) {
                for (int k = 0; k < m_ar; k++) {
                    phc[i*m_ar+k] += pha[i*m_ar+k]*phb[j*m_ar+k];
                }
            }
        }

        time2 = System.currentTimeMillis();
        double seconds = (time2 - time1) / 1000.0;

        System.out.printf("Time: %.3f seconds\n", seconds);

        System.out.println("Result matrix: ");
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < Math.min(10, m_br); j++) {
                System.out.print(phc[j] + " ");
            }
        }
        System.out.println();
    }

    public static void OnMultBlock(int m_ar, int m_br, int bkSize) {
        long time1, time2;

        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_br * m_br];
        double[] phc = new double[m_ar * m_ar];

        for (int i = 0; i < m_ar; i++)
            for (int j = 0; j < m_ar; j++)
                pha[i * m_ar + j] = 1.0;

        for (int i = 0; i < m_ar; i++)
            for (int j = 0; j < m_br; j++)
                phb[i * m_br + j] = i + 1;

        time1 = System.currentTimeMillis();

        for (int ib = 0; ib<m_ar;ib+=bkSize)
			for (int jb = 0; jb<m_ar;jb+=bkSize)
				for (int kb =0;kb<m_ar;kb+=bkSize)
					for (int i =ib;i<min(ib+bkSize,m_ar);i++)
						for (int j =jb;j<min(jb+bkSize,m_ar);j++)
							for (int k=kb;k<min(kb+bkSize,m_ar);k++)
								phc[i*m_ar+k] += pha[i*m_ar+k]*phb[j*m_ar+k];

        time2 = System.currentTimeMillis();
        double seconds = (time2 - time1) / 1000.0;

        System.out.printf("Time: %.3f seconds\n", seconds);

        System.out.println("Result matrix: ");
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < Math.min(10, m_br); j++) {
                System.out.print(phc[j] + " ");
            }
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int m_ar = 3000;
        int m_br = 600;
        int bkSize = 200;
        //OnMult(m_ar, m_ar);
        OnMultLine(m_ar, m_ar);
        //OnMultBlock(m_ar, m_br, bkSize);
    }
}
