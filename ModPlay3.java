
public class ModPlay3
{
	private static final int NUM_SAMPLES = 32;
	private static final int MAX_CHANNELS = 16;
	private static final int FIXED_POINT_SHIFT = 13;
	private static final int FIXED_POINT_ONE = 1 << FIXED_POINT_SHIFT;
	
	private static final short[] FINE_TUNE =
	{
		8192, 8251, 8311, 8371, 8432, 8493, 8555, 8617,
		7732, 7788, 7845, 7902, 7959, 8016, 8075, 8133
	};
	
	private static final short[] ARPEGGIO =
	{
		8192, 7732, 7298, 6889, 6502, 6137, 5793, 5468,
		5161, 4871, 4598, 4340, 4096, 3866, 3649, 3444
	};
	
	private int sampleRate;
	
	private String songName;
	private String[] instrumentNames = new String[ NUM_SAMPLES ];
	private int numChannels;
	private int songLength;
	private int numPatterns;
	private int c2Rate;
	private byte[] sequence;
	private byte[] patternData;
	private byte[][] sampleData = new byte[ NUM_SAMPLES ][];
	private byte[] sampleFineTune = new byte[ NUM_SAMPLES ];
	private byte[] sampleVolume = new byte[ NUM_SAMPLES ];
	private int[] sampleLoopStart = new int[ NUM_SAMPLES ];
	private int[] sampleLoopLength = new int[ NUM_SAMPLES ];
	private int[] channelInstrument = new int[ MAX_CHANNELS ];
	private int[] channelAssigned = new int[ MAX_CHANNELS ];
	private int[] channelEffect = new int[ MAX_CHANNELS ];
	private int[] channelParameter = new int[ MAX_CHANNELS ];
	private int[] channelVolume = new int[ MAX_CHANNELS ];
	private int[] channelPanning = new int[ MAX_CHANNELS ];
	private int[] channelPeriod = new int[ MAX_CHANNELS ];
	private int[] channelSamplePos = new int[ MAX_CHANNELS ];
	private int[] channelStep = new int[ MAX_CHANNELS ];
	private int[] channelArpeggio = new int[ MAX_CHANNELS ];
	
	private int tickSamplePos;
	private int tickSampleLen;
	
	private int currentSequencePos;
	private int nextSequencePos;
	private int currentRow;
	private int nextRow;
	private int currentTick;
	private int ticksPerRow = 6;
	private int tempo = 125;
	
	private int effectCounter;
	
	public ModPlay3( int sampleRate, java.io.InputStream moduleData ) throws java.io.IOException
	{
		this.sampleRate = sampleRate;
		songName = readString( moduleData, 20 );
System.out.println(songName);
		int[] sampleLengths = new int[ NUM_SAMPLES ];
		for( int idx = 1; idx < NUM_SAMPLES; idx++ )
		{
			instrumentNames[ idx ] = readString( moduleData, 22 );
			sampleLengths[ idx ] = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * 2;
			sampleFineTune[ idx ] = ( byte ) ( moduleData.read() & 0xF );
			sampleVolume[ idx ] = ( byte ) ( moduleData.read() & 0x7F );
			int loopStart = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * 2;
			int loopLength = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * 2;
			if( loopLength < 4 || loopStart > sampleLengths[ idx ] )
			{
				loopStart = sampleLengths[ idx ];
			}
			if( loopStart + loopLength > sampleLengths[ idx ] )
			{
				loopLength = sampleLengths[ idx ] - loopStart;
			}
			sampleLoopStart[ idx ]  = loopStart * FIXED_POINT_ONE;
			sampleLoopLength[ idx ]  = loopLength * FIXED_POINT_ONE;
if(instrumentNames[idx].length() > 0 )System.out.println(instrumentNames[idx]);
		}
		songLength = moduleData.read() & 0x7F;
		int restart = moduleData.read() & 0x7F;
		sequence = readBytes( moduleData, 128 );
		for( int idx = 0; idx < 128; idx++ )
		{
			if( numPatterns < sequence[ idx ] + 1 ) 
			{
				numPatterns = sequence[ idx ] + 1;
			}
		}
		String modType = readString( moduleData, 4 );
		if( modType.equals( "M.K." ) )
		{
			numChannels = 4;
			c2Rate = 8287;
		}
		else
		{
			throw new IllegalArgumentException( "Module not recognised!" );
		}
		patternData = readBytes( moduleData, numChannels * 4 * 64 * numPatterns );
		for( int idx = 1; idx < NUM_SAMPLES; idx++ )
		{
			sampleData[ idx ] = readBytes( moduleData, sampleLengths[ idx ] );
		}
		for( int chn = 0; chn < MAX_CHANNELS; chn += 4 )
		{
			channelPanning[ chn ] = channelPanning[ chn + 3 ] = FIXED_POINT_ONE / 4 * 3;
			channelPanning[ chn + 1 ] = channelPanning[ chn + 2 ] = FIXED_POINT_ONE / 4;
		}
	}
	
	public void getAudio( int[] output, int count )
	{
		for( int idx = 0, end = count * 2; idx < end; idx += 2 )
		{
			if( tickSamplePos++ >= tickSampleLen )
			{
				tick();
				tickSamplePos = 0;
				tickSampleLen = ( sampleRate * 5 ) / ( tempo * 2 );
			}
			int lamp = 0, ramp = 0;
			for( int chn = 0; chn < numChannels; chn++ )
			{
				int instrument = channelInstrument[ chn ];
				int loopLength = sampleLoopLength[ instrument ];
				int loopEnd = sampleLoopStart[ instrument ] + loopLength;
				int samplePos = channelSamplePos[ chn ];
				if( samplePos < loopEnd )
				{
					int amplitude = sampleData[ instrument ][ samplePos >> FIXED_POINT_SHIFT ] * channelVolume[ chn ];
					lamp += ( amplitude * ( FIXED_POINT_ONE - channelPanning[ chn ] ) ) >> FIXED_POINT_SHIFT;
					ramp += ( amplitude * channelPanning[ chn ] ) >> FIXED_POINT_SHIFT;
					samplePos += channelStep[ chn ];
					if( loopLength > 0 )
					{
						while( samplePos >= loopEnd )
						{
							samplePos -= loopLength;
						}
					}
					channelSamplePos[ chn ] = samplePos;
				}
			}
			output[ idx ] = lamp;
			output[ idx + 1 ] = ramp;
		}
	}
	
	private void row()
	{
		if( nextSequencePos < songLength )
		{
			currentSequencePos = nextSequencePos;
		}
		else
		{
			currentSequencePos = nextSequencePos = 0;
		}
		currentRow = nextRow;
		currentTick = ticksPerRow;
		nextRow = currentRow + 1;
		if( nextRow > 63 )
		{
			nextRow = 0;
			nextSequencePos = currentSequencePos + 1;
		}
		for( int chn = 0; chn < numChannels; chn++ )
		{
			int rowOffset = sequence[ currentSequencePos ] * 64 + currentRow;
			int patternDataOffset = ( rowOffset * numChannels + chn ) * 4;
			int period = ( ( patternData[ patternDataOffset ] & 0xF ) << 8 ) | ( patternData[ patternDataOffset + 1 ] & 0xFF );
			int instrument = ( patternData[ patternDataOffset ] & 0x10 ) | ( ( patternData[ patternDataOffset + 2 ] & 0xF0 ) >> 4 );
			int effect = patternData[ patternDataOffset + 2 ] & 0xF;
			int parameter = patternData[ patternDataOffset + 3 ] & 0xFF;
			if( effect == 0xE )
			{
				effect = 0xE0 | ( ( parameter >> 4 ) & 0xF );
				parameter = parameter & 0xF;
			}
			if( instrument > 0 )
			{
				channelAssigned[ chn ] = instrument;
				channelVolume[ chn ] = sampleVolume[ instrument ];
			}
			if( period > 0 )
			{
				channelInstrument[ chn ] = channelAssigned[ chn ];
				channelPeriod[ chn ] = period;
				channelSamplePos[ chn ] = 0;
			}
			channelEffect[ chn ] = effect;
			channelParameter[ chn ] = parameter;
			switch( effect )
			{
				case 0x0: /* Arpeggio. */
					break;
				case 0xC: /* Set Volume. */
					channelVolume[ chn ] = parameter;
					break;
				case 0xF: /* Set speed/tempo.*/
					if( channelParameter[ chn ] < 32 )
					{
						currentTick = ticksPerRow = channelParameter[ chn ];
					}
					else
					{
						tempo = channelParameter[ chn ];
					}
					break;
				default:
					throw new UnsupportedOperationException( "Unsupported effect 0x"
						+ ( Integer.toHexString( channelEffect[ chn ] )
						+ " " + Integer.toHexString( channelParameter[ chn ] ) ).toUpperCase() );
			}
		}
	}
	
	private void tick()
	{
		if( --currentTick <= 0 )
		{
			row();
			effectCounter = 0;
		}
		else
		{
			effectCounter++;
			for( int chn = 0; chn < numChannels; chn++ ) 
			{
				channelArpeggio[ chn ] = 0;
				switch( channelEffect[ chn ] )
				{
					case 0x0: /* Arpeggio. */
						if( channelParameter[ chn ] > 0 )
						{
							switch( effectCounter % 3 )
							{
								case 1:
									channelArpeggio[ chn ] = ( channelParameter[ chn ] >> 4 ) & 0xF;
									break;
								case 2:
									channelArpeggio[ chn ] = channelParameter[ chn ] & 0xF;
									break;
							}
						}
						break;
				}
			}
		}
		for( int chn = 0; chn < numChannels; chn++ ) 
		{
			/* Calculate volume and frequency. */
			if( channelVolume[ chn ] > 64 )
			{
				channelVolume[ chn ] = 64;
			}
			int period = channelPeriod[ chn ];
			if( period > 0 )
			{
				period = ( period * ARPEGGIO[ channelArpeggio[ chn ] ] ) >> FIXED_POINT_SHIFT;
				if( period < 28 )
				{
					period = 28;
				}
				int frequency = c2Rate * 428 / period;
				int fineTune = sampleFineTune[ channelInstrument[ chn ] ];
				frequency = ( frequency * FINE_TUNE[ fineTune ] ) >> FIXED_POINT_SHIFT;
				channelStep[ chn ] = frequency * FIXED_POINT_ONE / sampleRate;
			}
		}
	}
	
	private static byte[] readBytes( java.io.InputStream inputStream, int length ) throws java.io.IOException
	{
		byte[] bytes = new byte[ length ];
		int offset = 0;
		int count = 0;
		while( offset < length && count >= 0 )
		{
			offset += count;
			count = inputStream.read( bytes, offset, length - offset );
		}
		return bytes;
	}
	
	private static String readString( java.io.InputStream inputStream, int length ) throws java.io.IOException
	{
		byte[] bytes = readBytes( inputStream, length );
		length = 0;
		for( int idx = 0; idx < bytes.length; idx++ )
		{
			if( ( bytes[ idx ] & 0xFF ) <= 32 )
			{
				bytes[ idx ] = 32;
			}
			else
			{
				length = idx + 1;
			}
		}
		return new String( bytes, 0, length, "ISO-8859-1" );
	}
	
	public static void main( String[] args ) throws Exception {
		//for( int idx = 0; idx < 16; idx++ ) System.out.println( Math.round( Math.pow( 2, ( idx - 8 ) / 96.0 ) * FIXED_POINT_ONE ) );
		//for( int idx = 0; idx < 16; idx++ ) System.out.println( Math.round( Math.pow( 2, idx / -12.0 ) * FIXED_POINT_ONE ) );
		ModPlay3 modPlay3;
		java.io.InputStream inputStream = new java.io.FileInputStream( args[ 0 ] );
		try
		{
			modPlay3 = new ModPlay3( 48000, inputStream );
		}
		finally
		{
			inputStream.close();
		}
		javax.sound.sampled.AudioFormat audioFormat = new javax.sound.sampled.AudioFormat( 48000, 16, 2, true, false );
		javax.sound.sampled.SourceDataLine sourceDataLine = ( javax.sound.sampled.SourceDataLine )
			javax.sound.sampled.AudioSystem.getLine( new javax.sound.sampled.DataLine.Info(
				javax.sound.sampled.SourceDataLine.class, audioFormat ) );
		sourceDataLine.open( audioFormat );
		sourceDataLine.start();
		int[] mixBuf = new int[ 4096 ];
		byte[] outBuf = new byte[ mixBuf.length * 2 ];
		while( true )
		{
			modPlay3.getAudio( mixBuf, mixBuf.length / 2 );
			for( int idx = 0; idx < mixBuf.length; idx++ )
			{
				int ampl = mixBuf[ idx ];
				if( ampl > 32767 )
				{
					ampl = 32767;
				}
				if( ampl < -32768 )
				{
					ampl = -32768;
				}
				outBuf[ idx * 2 ] = ( byte ) ampl;
				outBuf[ idx * 2 + 1 ] = ( byte ) ( ampl >> 8 );
			}
			sourceDataLine.write( outBuf, 0, outBuf.length );
		}
	}
}
